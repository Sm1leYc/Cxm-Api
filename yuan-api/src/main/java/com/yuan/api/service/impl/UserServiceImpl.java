package com.yuan.api.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.api.constant.CommonConstant;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.utils.ThrowUtils;
import com.yuan.api.mapper.UserMapper;
import com.yuan.api.model.dto.apicallhistory.LoggingStatusRequest;
import com.yuan.api.model.dto.user.UserQueryRequest;
import com.yuan.api.model.dto.user.UserUpdatePwdRequest;
import com.yuan.api.model.enums.UserRoleEnum;
import com.yuan.api.model.vo.LoginUserVO;
import com.yuan.api.model.vo.UserVO;
import com.yuan.api.service.UserService;
import com.yuan.api.utils.DateUtils;
import com.yuan.api.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
//import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.User;
import com.yuan.api.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.yuan.api.constant.UserConstant.*;

/**
 * 用户服务实现
 *
 *
 */
@Service
@Slf4j
@RefreshScope
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    @Value("${encryption.salt}")
    private String SALT;

    @Value("${defaultAvatarUrl}")
    private String defaultAvatarUrl;

    public final UserMapper userMapper;

    public final RedisUtils redisUtils;

    public UserServiceImpl(UserMapper userMapper, RedisUtils redisUtils){
        this.userMapper = userMapper;
        this.redisUtils = redisUtils;
    }

    private static final String akPrefix = "ak-";
    private static final String skPrefix = "sk-";


    @Override
    public long userRegister(String username, String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "用户昵称过短");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "用户密码过短");
        }
        // 要求字母、数字、下划线，长度范围为3到20个字符
        String pattern = "^[a-zA-Z0-9_]{3,20}$";
        if (!userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "要求字母、数字、下划线，长度范围为3到20个字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "两次输入的密码不一致");
        }
        // 同步锁
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3.分配accessKey secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserName(username);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(akPrefix + accessKey);
            user.setSecretKey(skPrefix + secretKey);
            // 注册赠送100积分
            user.setPoints(DEFAULT_POINTS);
            user.setUserAvatar(defaultAvatarUrl);
            user.setLoggingEnabled(0);
            user.setStatus(1);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.ERROR_INTERNAL_SERVER, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user != null && user.getLoginFailCount() > 2){
            long minutedBetween = DateUtils.minuteBetween(user.getLastLoginTime(), new Date());
            // 未超过十分钟
            if (minutedBetween < 10) {
                throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "密码输入错误超过三次，请十分钟后重试或者联系管理员！");
            }
        }
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "用户不存在");
        }
        if ("admin".equals(user.getUserRole())){
            user.setUserRole("管理员");
        }
        // 密码不正确
        String userPasswordInDb = user.getUserPassword();
        if (!encryptPassword.equals(userPasswordInDb)){
            updateUserLoginFailCount(user, false);
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "密码不正确");
        }
        // 3. 记录用户的登录态
        updateUserLoginFailCount(user, true);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public boolean updatePwd(UserUpdatePwdRequest userUpdatePwdRequest, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        User user = new User();

        String userPassword = userUpdatePwdRequest.getUserPassword();
        String checkUserPassword = userUpdatePwdRequest.getCheckUserPassword();

        if (!userPassword.equals(checkUserPassword)){
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "两次输入的密码不一致");
        }

        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "用户密码过短");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        userUpdatePwdRequest.setUserPassword(encryptPassword);

        BeanUtils.copyProperties(userUpdatePwdRequest, user);
        user.setId(loginUser.getId());
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 更新登录成功或者失败时的数据库信息
     *
     * @param userInDb
     * @param flag 输入是否正确
     */
    public void updateUserLoginFailCount(User userInDb,Boolean flag){
        User user = new User();
        user.setId(userInDb.getId());
        if (flag){
            // 登录成功
            user.setLoginFailCount(0);
        }else {
            // 登录失败
            user.setLoginFailCount(userInDb.getLoginFailCount() + 1);
        }
        user.setLastLoginTime(new Date());
        this.baseMapper.updateById(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 从数据库查询最新用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        currentUser.setUserRole(UserRoleEnum.getTextByValue(currentUser.getUserRole()));
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && (UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()) || UserRoleEnum.ADMIN.getText().equals(user.getUserRole()));
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateSecretKey(Long id) {
        User user = this.getById(id);
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        user.setAccessKey(akPrefix + accessKey);
        user.setSecretKey(skPrefix + secretKey);
        return this.updateById(user);
    }

    @Override
    public boolean deductPoints(long userId, Integer requiredPoints) {
        int i = userMapper.deductPoints(userId, requiredPoints);
        return i > 0;
    }

    @Override
    public boolean hasEnoughPoints(long userId, Integer requiredPoints) {
        int points = userMapper.selectPoints(userId);
        // 判断剩余积分是否大于所需要的积分
        return points >= requiredPoints;
    }

    @Override
    public int updateLoggingStatus(LoggingStatusRequest loggingStatusRequest) {

        return userMapper.updateLoggingStatus(loggingStatusRequest.isLoggingEnabled(), loggingStatusRequest.getUserId());
    }
}
