package com.yuan.api.controller;

import com.yuan.api.common.BaseResponse;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.utils.TencentCosUtils;
import com.yupi.yuapicommon.common.ErrorCode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 *
 */
@RestController
@RequestMapping("/file")
@Slf4j
@Tag(name = "FileController")
public class FileController {


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {

        try {
            String url = TencentCosUtils.upLoadFile(multipartFile);

            return ResultUtils.success(url);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_ERROR, e.getMessage());
        }

    }

}
