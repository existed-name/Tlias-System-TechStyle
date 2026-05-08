package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.OperateLog;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.OperateLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.OperateLogSummaryVO;
import com.github.existed_name.tliaswebmanagement.mapper.OperateLogMapper;
import com.github.existed_name.tliaswebmanagement.service.OperateLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class OperateLogServiceImpl implements OperateLogService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OperateLogMapper operateLogMapper;


    // ==================== 构造器 ====================
    @Autowired
    public OperateLogServiceImpl( OperateLogMapper operateLogMapper ){
        this.operateLogMapper = operateLogMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public PageResult page( OperateLogQueryParam queryParam ){
        PageHelper.startPage( queryParam.getPage(), queryParam.getPageSize() );

        List< OperateLog > logList = operateLogMapper.selectByQuery( queryParam );
        Page< OperateLog > logPage = ( Page< OperateLog> ) logList;

        return new PageResult( logPage.getTotal(), logPage.getResult() );
    }

    @Override
    public OperateLogSummaryVO getSummaryData(){
        OperateLogSummaryVO summaryData = new OperateLogSummaryVO();

        summaryData.setTotalCount( operateLogMapper.countAll() );
        summaryData.setTodayCount( operateLogMapper.countToday() );
        summaryData.setTotalTypeCounts( operateLogMapper.countTotalOperateTypes() );
        summaryData.setAvgCostTimes( operateLogMapper.selectAvgCostTimes() );

        return summaryData;
    }


    // ==================== 私有辅助方法 ====================

}
