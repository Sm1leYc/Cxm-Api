package com.yuan.api.user;

import com.yuan.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

@SpringBootTest
public class UserTest {


    @Resource
    private UserService userService;

    @Test
    public void test1(){
        String userAccount = "test_";
        String password = "admin123";
        for (int i = 0; i < 1000; i++){
            userService.userRegister(userAccount + i,
                    userAccount + i,
                    password,
                    password);
        }

    }
}
