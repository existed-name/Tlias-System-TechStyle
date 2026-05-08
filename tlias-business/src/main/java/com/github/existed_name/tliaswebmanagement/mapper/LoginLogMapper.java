package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.LoginLog;
import com.github.existed_name.tlias_pojo.dto.LoginLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.LoginLogSummaryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginLogMapper {
    /**
     * 记录登录日志
     */
    @Insert( """
            INSERT INTO emp_login_log
                ( username, password, login_time, is_success, jwt, cost_time )
            VALUES ( #{username}, #{password}, #{loginTime}, #{isSuccess}, #{jwt}, #{costTime} )
            """ )
    void insert( LoginLog loginLog );

    /**
     * 条件分页查询( 登录日志 )
     */
    List< LoginLog > selectByQuery( LoginLogQueryParam queryParam );

    /**
     * 统计今天登录的人数
     */
    Long countTodayLogin();

    /**
     * 统计今天登录失败的人数
     */
    Long countTodayFail();

    /**
     * 统计累计登录成功、失败的人数( 饼图 )
     */
    List< LoginLogSummaryVO.PieChartVO> countTotalSuccessFail();

    /**
     * 统计累计登录失败前 5 的用户名及其失败次数( 柱状图 )
     */
    List< LoginLogSummaryVO.FailRankVO> selectFailRank();
}
