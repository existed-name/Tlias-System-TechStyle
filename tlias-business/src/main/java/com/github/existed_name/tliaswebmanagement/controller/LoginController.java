package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.LoginInfo;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoginController {

    private final EmpService empService;

    @Autowired
    public LoginController( EmpService empService ){
        this.empService = empService;
    }

    /*
    http://localhost:8080/login
    请求数据样例
    {
    "username": "wuyong",
    "password": "wuyong"
    }
    */
    @PostMapping("/login")
    public Result< Object > login( @RequestBody Emp emp ){
        log.info("员工来登录啦 , {}", emp);
        LoginInfo loginInfo = empService.login(emp);
        if(loginInfo != null){
            return Result.success(loginInfo);
        }
        return Result.error("用户名或密码错误~");
    }

}