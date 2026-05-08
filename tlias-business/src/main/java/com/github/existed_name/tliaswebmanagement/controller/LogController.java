package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_pojo.dto.LoginLogQueryParam;
import com.github.existed_name.tlias_pojo.dto.OperateLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.LoginLogSummaryVO;
import com.github.existed_name.tlias_pojo.vo.OperateLogSummaryVO;
import com.github.existed_name.tliaswebmanagement.service.LoginLogService;
import com.github.existed_name.tliaswebmanagement.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping( "/logs" )
public class LogController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OperateLogService operateLogService;
    private final LoginLogService loginLogService;


    // ==================== 构造器 ====================
    @Autowired
    public LogController( OperateLogService operateLogService, LoginLogService loginLogService ){
        this.operateLogService = operateLogService;
        this.loginLogService = loginLogService;
    }


    // ==================== 公有方法 ====================
    /**
     * 操作日志信息的分页查询
     */
    @GetMapping( "/operation" )
    public Result< PageResult > page( OperateLogQueryParam queryParam ){
        log.info( "分页查询操作日志, 查询参数 {}", queryParam );
        PageResult result = operateLogService.page( queryParam );
        return Result.success( result );
    }

    /**
     * 操作日志数据统计
     */
    @GetMapping( "/operation/summary" )
    public Result< OperateLogSummaryVO > getOperateLogSummary(){
        log.info( "获取操作日志统计数据" );
        OperateLogSummaryVO operateLogSummary = operateLogService.getSummaryData();
        return Result.success( operateLogSummary );
    }

    /**
     * 登录日志分页查询
     */
    @GetMapping( "/login" )
    public Result< PageResult > page( LoginLogQueryParam queryParam ){
        log.info( "分页查询登录日志, 查询参数 {}", queryParam );
        PageResult result = loginLogService.page( queryParam );
        return Result.success( result );
    }

    /**
     * 登录日志数据统计
     */
    @GetMapping( "/login/summary" )
    public Result< LoginLogSummaryVO > getLoginLogSummary(){
        log.info( "获取登录日志统计数据" );
        LoginLogSummaryVO loginLogSummary = loginLogService.getSummaryData();
        return Result.success( loginLogSummary );
    }



    // ==================== 私有辅助方法 ====================

}
