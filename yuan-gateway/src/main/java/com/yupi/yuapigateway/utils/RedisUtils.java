package com.yupi.yuapigateway.utils;


import com.google.common.collect.HashMultimap;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;


    // ============================Lock=============================

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的key
     * @param waitTime 获取锁的等待时间(毫秒)
     * @param leaseTime 锁的持有时间(毫秒)
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        try {
            // 使用Redisson客户端获取锁
            RLock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // ============================String=============================

    /**
     * 写入缓存
     *
     * @param key   redis键
     * @param value redis值
     * @return 是否成功
     */
    public  boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key   redis键
     * @param value redis值
     * @return 是否成功
     */
    public  boolean set(final String key, String value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);
            stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置失效时间
     * @param key redis键
     * @param value redis值
     * @param expireTime 失效时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public  boolean set(final String key, String value, Long expireTime,TimeUnit timeUnit) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value,expireTime,timeUnit);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 设置过期时间带偏移量
    public boolean setWithRandomOffset(final String key, String value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);

            // 添加随机偏移量
            long randomOffset = (long)(expireTime * 0.05 * Math.random()); // 5%随机偏移
            long finalExpire = expireTime + randomOffset;

            stringRedisTemplate.expire(key, finalExpire, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 设置key有效期
     * @param key
     * @param expireTime
     * @param timeUnit
     * @return
     */
    public  boolean expire(final String key,Long expireTime,TimeUnit timeUnit){
        boolean result = false;
        try {
            stringRedisTemplate.expire(key, expireTime, timeUnit);
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * key自增
     * @param key redis键
     * @param delta 自增数
     */
    public  boolean increment(final String key,Long delta){
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.increment(key,delta);
            result = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    /**
     * 批量删除Redis key
     *
     * @param pattern 键名包含字符串（如：myKey*）
     */
    public  void removePattern(final String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && keys.size() > 0) {
            stringRedisTemplate.delete(keys);
        }
    }

    public void safeRemovePattern(final String pattern) {
        // 使用SCAN分批次获取匹配的键
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
                (RedisConnection connection) -> connection.scan(options))) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        }

        if (!keys.isEmpty()) {
            // 分批次删除，每批处理100个键，防止bigkey
            List<String> keyList = new ArrayList<>(keys);
            int batchSize = 100;
            for (int i = 0; i < keyList.size(); i += batchSize) {
                List<String> batch = keyList.subList(i, Math.min(i + batchSize, keyList.size()));
                // 异步删除，不阻塞主线程
                stringRedisTemplate.unlink(batch);
            }
        }
    }

    /**
     * 删除key,也删除对应的value
     *
     * @param key Redis键名
     */
    public  void remove(final String key) {
        if (exists(key)) {
            stringRedisTemplate.delete(key);
        }
    }

    // 获取key大小
    public Long getKeySize(String key) {
        return stringRedisTemplate.execute(
                new DefaultRedisScript<>("return redis.call('MEMORY', 'USAGE', KEYS[1])", Long.class),
                Collections.singletonList(key)
        );
    }

    // 针对bigkey，判断是否异步删除
    public void smartDelete(String key) {
        try {
            Long size = getKeySize(key);

            if (size == null || size <= 0) {
                return; // Key不存在
            }

            if (size < 10 * 1024) { // 小于10KB
                stringRedisTemplate.delete(key); // 同步删除
            } else {
                stringRedisTemplate.unlink(key); // 异步删除

                // 记录大Key删除监控
                log.info("Deleted big key: {} (size: {} bytes)", key, size);
            }
        } catch (Exception e) {
            log.error("删除Key失败: {}", key, e);
        }
    }


    /**
     * 判断缓存中是否有对应的value
     *
     * @param key Redis键名
     * @return 是否存在
     */
    public  Boolean exists(final String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 获取并计算 Redis 键的剩余生存时间，并返回格式化后的时间信息
     *
     * @param redisKey Redis键
     * @return 格式化后的剩余时间信息
     */
    public  String calculateTTL(String redisKey) {
        Long ttl = stringRedisTemplate.getExpire(redisKey);

        // 如果键不存在或者TTL为负数，返回默认信息
        if (ttl == null || ttl <= 0) {
            return "未知时间";
        }

        return formatTimeMessage(ttl);
    }

    /**
     * 根据剩余时间格式化输出消息
     *
     * @param ttl 剩余时间（秒）
     * @return 格式化后的时间信息
     */
    public  String formatTimeMessage(Long ttl) {
        long hours = ttl / 3600;
        long minutes = (ttl % 3600) / 60;
        long seconds = ttl % 60;

        StringBuilder timeMessage = new StringBuilder();
        if (hours > 0) {
            timeMessage.append(hours).append("小时");
        }
        if (minutes > 0) {
            timeMessage.append(minutes).append("分钟");
        }
        if (seconds > 0) {
            timeMessage.append(seconds).append("秒");
        }

        return timeMessage.length() > 0 ? timeMessage.toString() : "少于1秒";
    }


    /**
     * 读取缓存
     *
     * @param key Redis键名
     * @return 是否存在
     */
    public  String get(final String key) {
        String result = null;
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 哈希 添加
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @param value   哈希值
     */
    public  void hmSet(String key, String hashKey, String value) {
        HashOperations<String, String, String> hash = stringRedisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    /**
     * 哈希获取数据
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @return 哈希值
     */
    public  String hmGet(String key, String hashKey) {
        HashOperations<String, String, String> hash = stringRedisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 判断hash是否存在键
     *
     * @param key     Redis键
     * @param hashKey 哈希键
     * @return 是否存在
     */
    public  boolean hmHasKey(String key, String hashKey) {
        HashOperations<String, String, String> hash = stringRedisTemplate.opsForHash();
        return hash.hasKey(key, hashKey);
    }

    /**
     * 删除hash中一条或多条数据
     *
     * @param key      Redis键
     * @param hashKeys 哈希键名数组
     * @return 删除数量
     */
    public  long hmRemove(String key, String... hashKeys) {
        HashOperations<String, String, String> hash = stringRedisTemplate.opsForHash();
        return hash.delete(key, hashKeys);
    }

    /**
     * 获取所有哈希键值对
     *
     * @param key Redis键名
     * @return 哈希Map
     */
    public  Map<String, String> hashMapGet(String key) {
        HashOperations<String, String, String> hash = stringRedisTemplate.opsForHash();
        return hash.entries(key);
    }

    /**
     * 保存Map到哈希
     *
     * @param key Redis键名
     * @param map 哈希Map
     */
    public  void hashMapSet(String key, Map<String, Object> map) {
        HashOperations<String, String, Object> hash = stringRedisTemplate.opsForHash();
        hash.putAll(key, map);
    }

    /**
     * 列表-追加值
     *
     * @param key   Redis键名
     * @param value 列表值
     */
    public  void lPush(String key, String value) {
        ListOperations<String, String> list = stringRedisTemplate.opsForList();
        list.leftPush(key, value);
    }

    /**
     * 列表-删除值
     *
     * @param key   Redis键名
     * @param value 列表值
     */
    public  void lRemove(String key, String value) {
        ListOperations<String, String> list = stringRedisTemplate.opsForList();
        list.remove(key, 0, value);
    }




    /**
     * 列表-获取指定范围数据
     *
     * @param key   Redis键名
     * @param start 开始行号（start:0，end:-1查询所有值）
     * @param end   结束行号
     * @return 列表
     */
    public  List<String> lRange(String key, long start, long end) {
        ListOperations<String, String> list = stringRedisTemplate.opsForList();
        return list.range(key, start, end);
    }
    /**
     * 集合添加
     *
     * @param key   Redis键名
     * @param value 值
     */
    public  void add(String key, String value) {
        SetOperations<String, String> set = stringRedisTemplate.opsForSet();
        set.add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key Redis键名
     * @return 集合
     */
    public  Set<String> setMembers(String key) {
        SetOperations<String, String> set = stringRedisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key   Redis键名
     * @param value 值
     * @param score 排序号
     */
    public  void zAdd(String key, String value, double score) {
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        zSet.add(key, value, score);
    }

    /**
     * 有序集合-获取指定范围
     *
     * @param key        Redis键
     * @param startScore 开始序号
     * @param endScore   结束序号
     * @return 集合
     */
    public  Set<String> rangeByScore(String key, double startScore, double endScore) {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        return zset.rangeByScore(key, startScore, endScore);
    }

    /**
     * 模糊查询Redis键名
     *
     * @param pattern 键名包含字符串（如：myKey*）
     * @return 集合
     */
    public  Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    /**
     * 获取多个hashMap
     *
     * @param keySet
     * @return List<Map < String, String>> hashMap列表
     */
    public  List hashMapList(Collection<String> keySet) {
        return stringRedisTemplate.executePipelined(new SessionCallback<String>() {
            @Override
            public <K, V> String execute(RedisOperations<K, V> operations) throws DataAccessException {
                HashOperations hashOperations = operations.opsForHash();
                for (String key : keySet) {
                    hashOperations.entries(key);
                }
                return null;
            }
        });
    }

    /**
     * 保存多个哈希表（HashMap）(Redis键名可重复)
     *
     * @param batchMap Map<Redis键名,Map<键,值>>
     */
    public  void batchHashMapSet(HashMultimap<String, Map<String, String>> batchMap) {
        // 设置5秒超时时间
        stringRedisTemplate.expire("max", 25, TimeUnit.SECONDS);
        stringRedisTemplate.executePipelined(new RedisCallback<List<Map<String, String>>>() {

            @Override
            public List<Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                Iterator<Map.Entry<String, Map<String, String>>> iterator = batchMap.entries().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Map<String, String>> hash = iterator.next();
                    // 哈希名,即表名
                    byte[] hashName = stringRedisTemplate.getStringSerializer().serialize(hash.getKey());
                    Map<String, String> hashValues = hash.getValue();
                    Iterator<Map.Entry<String, String>> it = hashValues.entrySet().iterator();
                    // 将元素序列化后缓存，即表的多条哈希记录
                    Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                    while (it.hasNext()) {
                        // hash中一条key-value记录
                        Map.Entry<String, String> entry = it.next();
                        byte[] key = stringRedisTemplate.getStringSerializer().serialize(entry.getKey());
                        byte[] value = stringRedisTemplate.getStringSerializer().serialize(entry.getValue());
                        hashes.put(key, value);
                    }
                    // 批量保存
                    connection.hMSet(hashName, hashes);
                }
                return null;
            }
        });
    }

    /**
     * 保存多个哈希表（HashMap）(Redis键名不可以重复)
     *
     * @param dataMap Map<Redis键名,Map<哈希键,哈希值>>
     */
    public  void batchHashMapSet(Map<String, Map<String, String>> dataMap) {
        // 设置5秒超时时间
        stringRedisTemplate.expire("max", 25, TimeUnit.SECONDS);
        stringRedisTemplate.executePipelined(new RedisCallback<List<Map<String, String>>>() {

            @Override
            public List<Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                Iterator<Map.Entry<String, Map<String, String>>> iterator = dataMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Map<String, String>> hash = iterator.next();
                    // 哈希名,即表名
                    byte[] hashName = stringRedisTemplate.getStringSerializer().serialize(hash.getKey());
                    Map<String, String> hashValues = hash.getValue();
                    Iterator<Map.Entry<String, String>> it = hashValues.entrySet().iterator();
                    // 将元素序列化后缓存，即表的多条哈希记录
                    Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                    while (it.hasNext()) {
                        // hash中一条key-value记录
                        Map.Entry<String, String> entry = it.next();
                        byte[] key = stringRedisTemplate.getStringSerializer().serialize(entry.getKey());
                        byte[] value = stringRedisTemplate.getStringSerializer().serialize(entry.getValue());
                        hashes.put(key, value);
                    }
                    // 批量保存
                    connection.hMSet(hashName, hashes);
                }
                return null;
            }
        });
    }

    /**
     * 保存多个哈希表（HashMap）列表（哈希map的Redis键名不能重复）
     *
     * @param list Map<Redis键名,Map<哈希键,哈希值>>
     * @see RedisUtils*.batchHashMapSet()*
     */
    public  void batchHashMapListSet(List<Map<String, Map<String, String>>> list) {
        // 设置5秒超时时间
        stringRedisTemplate.expire("max", 25, TimeUnit.SECONDS);
        stringRedisTemplate.executePipelined(new RedisCallback<List<Map<String, String>>>() {

            @Override
            public List<Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                for (Map<String, Map<String, String>> dataMap : list) {
                    Iterator<Map.Entry<String, Map<String, String>>> iterator = dataMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Map<String, String>> hash = iterator.next();
                        // 哈希名,即表名
                        byte[] hashName = stringRedisTemplate.getStringSerializer().serialize(hash.getKey());
                        Map<String, String> hashValues = hash.getValue();
                        Iterator<Map.Entry<String, String>> it = hashValues.entrySet().iterator();
                        // 将元素序列化后缓存，即表的多条哈希记录
                        Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                        while (it.hasNext()) {
                            // hash中一条key-value记录
                            Map.Entry<String, String> entry = it.next();
                            byte[] key = stringRedisTemplate.getStringSerializer().serialize(entry.getKey());
                            byte[] value = stringRedisTemplate.getStringSerializer().serialize(entry.getValue());
                            hashes.put(key, value);
                        }
                        // 批量保存
                        connection.hMSet(hashName, hashes);
                    }
                }
                return null;
            }
        });
    }

}

