package com.yuan.api.model.dto.user;

import lombok.Data;

@Data
public class UserUpdatePwdRequest {


    private String userPassword;

    private String checkUserPassword;
}
