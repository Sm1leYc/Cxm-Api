package com.yuan.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.api.annotation.AuthCheck;
import com.yuan.api.common.*;
import com.yuan.api.constant.UserConstant;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.utils.ThrowUtils;
import com.yuan.api.model.dto.user.*;
import com.yuan.api.model.enums.UserRoleEnum;
import com.yuan.api.common.BaseResponse;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.User;
import com.yuan.api.model.vo.LoginUserVO;
import com.yuan.api.model.vo.UserVO;
import com.yuan.api.service.UserService;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import static com.yuan.api.constant.UserConstant.*;

/**
 * 用户接口
 *
 *
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "UserController")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        String username = userRegisterRequest.getUserName();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(username, userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest, request, response);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        user.setUserRole(UserRoleEnum.getTextByValue(user.getUserRole()));
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.ERROR_INVALID_PARAMETER);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新密码
     * @param userUpdatePwdRequest
     * @param request
     * @return
     */
    @PostMapping("/update/pwd")
    public BaseResponse<Boolean> updatePwd(@RequestBody UserUpdatePwdRequest userUpdatePwdRequest,
                                              HttpServletRequest request) {
        if (userUpdatePwdRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        return ResultUtils.success(userService.updatePwd(userUpdatePwdRequest, request));
    }

    /**
     * 更新ak/sk
     * @param userId
     * @param request
     * @return
     */
    @PostMapping("/update/updateSk")
    public BaseResponse<Boolean> updateSk(Long userId,
                                           HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        return ResultUtils.success(userService.updateSecretKey(userId));
    }

    /**
     * 封禁用户
     * @param userId
     * @return
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> banUser(Long userId, Integer status){
        if (userId == null){
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }

        User user = userService.getById(userId);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setStatus(status);

        return ResultUtils.success(userService.updateById(updatedUser));
    }


    /**
     * 签到
     * @param userId
     * @return
     */
    @PostMapping("/signIn")
    public BaseResponse<Boolean> signIn(@RequestParam Long userId){

        User user = userService.getById(userId);

        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        Date today = new Date();
        // 检查用户是否已经签到
        if (user.getLastSignIn() != null && isSameDay(user.getLastSignIn(), today)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "每天只能签到一次，请勿重复签到！");
        }

        // 更新用户的最后签到日期和积分
        user.setLastSignIn(today);
        int points = user.getPoints() == null ? 0 : user.getPoints();

        if (points + ADD_POINTS > MAX_POINTS){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "累计积分暂时不可超过" + MAX_POINTS + "，请消耗一定积分后进行签到");
        }

        user.setPoints(points + ADD_POINTS);

        // 更新用户信息
        userService.updateById(user);

        return ResultUtils.success(true);
    }

    // 检查两个日期是否为同一天
    private boolean isSameDay(Date date1, Date date2) {
        return date1.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .isEqual(date2.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
    }

}
