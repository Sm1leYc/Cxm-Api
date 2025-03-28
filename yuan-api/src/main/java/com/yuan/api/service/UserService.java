package com.yuan.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.api.model.dto.apicallhistory.LoggingStatusRequest;
import com.yuan.api.model.dto.user.UserQueryRequest;
import com.yuan.api.model.dto.user.UserUpdateMyRequest;
import com.yuan.api.model.dto.user.UserUpdatePwdRequest;
import com.yupi.yuapicommon.model.entity.User;
import com.yuan.api.model.vo.LoginUserVO;
import com.yuan.api.model.vo.UserVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String username, String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean updatePwd(UserUpdatePwdRequest userUpdatePwdRequest, HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 更新ak sk
     * @param id
     * @return
     */
    boolean updateSecretKey(Long id);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 更新积分
     * @param userId
     * @param requiredPoints
     * @return
     */
    boolean deductPoints(long userId, Integer requiredPoints);

    /**
     * 是否有足够积分
     * @param userId
     * @param requiredPoints
     * @return
     */
    boolean hasEnoughPoints(long userId, Integer requiredPoints);

    /**
     * 更新用户记录日志状态
     * @param loggingStatusRequest
     * @return
     */
    int updateLoggingStatus(LoggingStatusRequest loggingStatusRequest);
}
