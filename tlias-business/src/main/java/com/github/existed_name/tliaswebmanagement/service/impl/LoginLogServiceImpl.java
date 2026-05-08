package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.LoginLog;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.LoginLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.LoginLogSummaryVO;
import com.github.existed_name.tliaswebmanagement.mapper.LoginLogMapper;
import com.github.existed_name.tliaswebmanagement.service.LoginLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class LoginLogServiceImpl implements LoginLogService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final LoginLogMapper loginLogMapper;


    // ==================== 构造器 ====================
    @Autowired
    public LoginLogServiceImpl( LoginLogMapper loginLogMapper ){
        this.loginLogMapper = loginLogMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public PageResult page( LoginLogQueryParam queryParam ){
        PageHelper.startPage( queryParam.getPage(), queryParam.getPageSize() );

        List< LoginLog > logList = loginLogMapper.selectByQuery( queryParam );
        Page< LoginLog > logPage = ( Page< LoginLog> ) logList;

        return new PageResult( logPage.getTotal(), logPage.getResult() );
    }

    @Override
    public LoginLogSummaryVO getSummaryData(){
        LoginLogSummaryVO summaryData = new LoginLogSummaryVO();

        summaryData.setTodayLoginCount( loginLogMapper.countTodayLogin() );
        summaryData.setTodayFailCount( loginLogMapper.countTodayFail() );
        summaryData.setTotalSuccessFail( loginLogMapper.countTotalSuccessFail() );
        summaryData.setFailRank( loginLogMapper.selectFailRank() );

        return summaryData;
    }


    // ==================== 私有辅助方法 ====================

}
