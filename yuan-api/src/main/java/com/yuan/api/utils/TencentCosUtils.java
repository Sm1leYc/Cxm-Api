package com.yuan.api.utils;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 腾讯云COS管理
 */
@Slf4j
@Component
public class TencentCosUtils {


    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "gif", "webp"));
    private static final String UPLOAD_DIR = "images/";


    /**
     * 1.调用静态方法getCosClient()就会获得COSClient实例
     * 2.本方法根据永久密钥初始化 COSClient的，官方是不推荐，官方推荐使用临时密钥，是可以限制密钥使用权限，创建cred时有些区别
     *
     * @return COSClient实例
     */
    public static COSClient getCosClient() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        String secretId = SpringContextUtils.getProperty("tencentCOS.secretId");
        String secretKey = SpringContextUtils.getProperty("tencentCOS.secretKey");
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2.1 设置存储桶的地域（上文获得）
        String bucketAddr = SpringContextUtils.getProperty("tencentCOS.bucketAddr");
        Region region = new Region(bucketAddr);
        ClientConfig clientConfig = new ClientConfig(region);
        // 2.2 使用https协议传输
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 返回COS客户端
        return cosClient;
    }

    /**
     * 只要调用静态方法upLoadFile(MultipartFile multipartFile)就可以获取上传后文件的全路径
     *
     * @param file
     * @return 返回文件的浏览全路径
     */
    public static String upLoadFile(MultipartFile file) {

        // 获取文件的原始名称
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.UPLOAD_ERROR, "文件名不能为空");
        }

        // 获取文件的类型（扩展名）
        String fileType = FileUtil.getSuffix(originalFilename);

        // 检查文件格式是否允许
        boolean isAllowedExtension = ALLOWED_EXTENSIONS.contains(fileType);
        if (!isAllowedExtension) {
            throw new BusinessException(ErrorCode.UPLOAD_ERROR, "不支持的文件格式：" + fileType);
        }

        // 检查文件大小是否超过限制
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.UPLOAD_ERROR, "文件最大上传大小为 2MB!");
        }
        String url = "";

        COSClient cosClient = null;

        try {

            cosClient = getCosClient();
            // 获取上传的文件的输入流
            InputStream inputStream = file.getInputStream();

            // 避免文件覆盖，生成唯一文件名
            String fileName = UUID.randomUUID().toString() + fileType;

            // 指定文件上传到 COS 上的路径
            String key = UPLOAD_DIR + fileName;

            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(inputStream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setContentType(fileType);

            // 上传文件
            String rootSrc = SpringContextUtils.getProperty("tencentCOS.rootSrc");
            String bucketName = SpringContextUtils.getProperty("tencentCOS.bucketName");
            PutObjectResult putResult = getCosClient().putObject(bucketName, key, inputStream, objectMetadata);

            // 创建文件的网络访问路径
            url = rootSrc + key;

        } catch (Exception e){
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.UPLOAD_ERROR, e.getMessage());
        } finally {
            // 确保在finally块中关闭客户端，释放资源
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }

        return url;

    }



}
