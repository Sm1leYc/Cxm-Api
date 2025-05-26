package com.yuan.api.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.api.common.BaseResponse;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.model.dto.feedback.listFeedbackRequest;
import com.yuan.api.model.dto.feedback.updateFeedbackRequest;
import com.yuan.api.model.dto.interfaceinfo.FeedbackInterfaceInfoRequest;
import com.yuan.api.model.entity.Feedback;
import com.yuan.api.model.vo.FeedbackVO;
import com.yuan.api.service.FeedbackService;
import com.yuan.api.service.UserService;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import static com.yuan.api.constant.UserConstant.ADMIN_ROLE;


/**
 * 反馈
 */
@RestController
@RequestMapping("/feedback")
@Slf4j
@Tag(name = "FeedbackController")
public class FeedbackController {

    private final FeedbackService feedbackService;

    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService, UserService userService){
        this.feedbackService = feedbackService;
        this.userService = userService;
    }


    /**
     * 反馈接口（用户使用）
     *
     * @param  /
     * @return /
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> feedbackInterfaceInfo(@RequestBody FeedbackInterfaceInfoRequest feedbackInterfaceInfoRequest, HttpServletRequest request) {
        if (feedbackInterfaceInfoRequest == null){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        User loginUser = userService.getLoginUser(request);

        Feedback feedback = new Feedback();
        feedback.setUserAccount(loginUser.getUserAccount());
        feedback.setFeedbackType(feedbackInterfaceInfoRequest.getFeedbackType());
        feedback.setFeedbackContent(feedbackInterfaceInfoRequest.getFeedbackContent());
        feedback.setContact(feedbackInterfaceInfoRequest.getContact());
        feedback.setCreateTime(LocalDateTime.now());

        boolean save = feedbackService.save(feedback);

        return ResultUtils.success(save);
    }

    @PostMapping("/get")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<FeedbackVO> getFeedbackById(@RequestParam Long id) {
        if (id == null || id < 0){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        Feedback byId = feedbackService.getById(id);

        FeedbackVO feedbackVO = new FeedbackVO();
        feedbackVO.setId(byId.getId());
        feedbackVO.setFeedbackType(byId.getFeedbackType());
        feedbackVO.setFeedbackContent(byId.getFeedbackContent());
        feedbackVO.setContact(byId.getContact());
        feedbackVO.setCreateTime(byId.getCreateTime());
        feedbackVO.setStatus(byId.getStatus());

        feedbackVO.setUserAccount(byId.getUserAccount());

        return ResultUtils.success(feedbackVO);

    }

    @PostMapping("/list/page")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Page<Feedback>> listFeedbackByPage(@RequestBody listFeedbackRequest listFeedbackRequest){

        // 获取分页参数
        long current = listFeedbackRequest.getCurrent();
        long size = listFeedbackRequest.getPageSize();

        Page<Feedback> feedbackPage = new Page<>(current, size);

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("createTime");

        Page<Feedback> result = feedbackService.page(feedbackPage, queryWrapper);

        // 返回分页结果
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Boolean> updateFeedback(@RequestBody updateFeedbackRequest updateFeedbackRequest) {
        if (updateFeedbackRequest == null || updateFeedbackRequest.getId() < 0){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        Long id = updateFeedbackRequest.getId();
        Feedback byId = feedbackService.getById(id);

        if (byId == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        Feedback feedback = new Feedback();
        feedback.setId(id);
        feedback.setStatus(updateFeedbackRequest.getFeedbackStatusEnum());

        boolean b = feedbackService.updateById(feedback);

        return ResultUtils.success(b);

    }

    @PostMapping("/delete")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Boolean> deleteFeedback(@RequestParam Long id) {
        if (id == null || id < 0){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        return ResultUtils.success(feedbackService.removeById(id));
    }



}
