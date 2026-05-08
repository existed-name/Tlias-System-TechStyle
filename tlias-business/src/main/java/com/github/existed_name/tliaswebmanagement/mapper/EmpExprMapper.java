package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.EmpExpr;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmpExprMapper {
    /**
     * 插入员工 单次 工作经历
     */
    @Insert( "INSERT INTO tlias.emp_expr " +
            "( emp_id, begin, end, company, job ) " +
            "VALUES( #{empId}, #{begin}, #{end}, #{company}, #{job} )" )
    void insert( EmpExpr empExpr );

    /**
     * 批量插入员工工作经历信息
     */
    void insertBatch( List< EmpExpr > exprList );

    /**
     * 根据多个员工id批量删除对应的工作经历
     *
     * @param empIds 员工id数组
     */
    void deleteBatchByEmpIds( @Param( "empIds" ) List< Integer > empIds );

    /**
     * 根据员工id查询工作经历表
     */
    @Select( """
            SELECT *
            FROM emp_expr
            WHERE emp_id = #{empId}
            ORDER BY begin DESC
            """ )
    List< EmpExpr > findByEmpId( @Param( "empId" ) Integer empId );

}
