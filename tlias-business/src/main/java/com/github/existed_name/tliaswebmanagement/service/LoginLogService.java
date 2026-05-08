package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.LoginLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.LoginLogSummaryVO;
import jakarta.validation.constraints.NotNull;

public interface LoginLogService {
    /**
     * 分页查询操作日志
     */
    PageResult page( @NotNull( message = "登录日志查询参数不能为空" ) LoginLogQueryParam queryParam );

    /**
     * 操作日志数据统计
     */
    LoginLogSummaryVO getSummaryData();
}
