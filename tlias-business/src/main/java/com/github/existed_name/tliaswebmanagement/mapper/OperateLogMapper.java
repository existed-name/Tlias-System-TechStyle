package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.OperateLog;
import com.github.existed_name.tlias_pojo.dto.OperateLogQueryParam;
import com.github.existed_name.tlias_pojo.vo.OperateLogSummaryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperateLogMapper {
    /**
     * 插入日志数据
     */
    @Insert( """
            insert into operate_log 
                ( operate_emp_id, operate_time, class_name, method_name, method_params, return_value, cost_time ) 
            values ( #{operateEmpId}, #{operateTime}, #{className}, #{methodName}, #{methodParams}, #{returnValue}, #{costTime} )
            """ )
    void insert( OperateLog operateLog );

    /**
     * 条件查询( PageHelper 分页 )
     */
    List< OperateLog > selectByQuery( OperateLogQueryParam queryParam );

    /**
     * 累计操作总数
     */
    @Select( "SELECT count( 0 ) FROM operate_log" )
    Long countAll();

    /**
     * 今日操作总数
     */
    @Select( """
            SELECT count( 0 ) FROM operate_log
            WHERE operate_time >= curdate()
                AND operate_time < DATE_ADD( CURDATE(), INTERVAL 1 DAY )
            """ )
    Long countToday();

    /**
     * 今日各种操作类型的数量
     */
    List< OperateLogSummaryVO.TypeCountVO> countTotalOperateTypes();

    /**
     * 累计各种操作类型的平均耗时
     */
    List< OperateLogSummaryVO.AvgCostVO> selectAvgCostTimes();

}
