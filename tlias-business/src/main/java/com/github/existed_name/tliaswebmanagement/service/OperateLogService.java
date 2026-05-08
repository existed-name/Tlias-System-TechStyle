package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.OperateLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.OperateLogSummaryVO;
import jakarta.validation.constraints.NotNull;

public interface OperateLogService {
    /**
     * 分页查询操作日志
     */
    PageResult page( @NotNull( message = "操作日志查询参数不能为空" ) OperateLogQueryParam queryParam );

    /**
     * 操作日志数据统计
     */
    OperateLogSummaryVO getSummaryData();
}
