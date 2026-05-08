package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.dto.EmpQueryParam;
import com.github.existed_name.tlias_pojo.vo.JobCountVO;
import com.github.existed_name.tlias_pojo.vo.OptionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmpMapper {
    /**
     * 查询所有的员工及其对应的部门名称
     */
    @Select( """
            SELECT
                e.id, e.username, e.name, e.gender, e.phone, e.job, e.salary, e.image, e.entry_date, e.dept_id, e.create_time, e.update_time,
                d.name AS deptName
            FROM emp AS e
                LEFT JOIN dept AS d
                ON e.dept_id = d.id
            ORDER BY CONVERT( d.name USING gbk ) ASC, CONVERT( e.name USING gbk ) ASC, e.id ASC
            """ )
    List< Emp > selectAll();

    /**
     * 条件分页查询员工及其对应的部门名称
     */
    List< Emp > selectByQuery( EmpQueryParam queryParam );

    /**
     * 新增员工数据
     */
    @Options(useGeneratedKeys = true, // useGeneratedKeys = true → 开启获取数据库自增主键
            keyProperty = "id") // keyProperty = "id" → 把生成的主键回填到 dept.id
    @Insert( """
            insert into emp(username, name, gender, phone, job, salary, image, entry_date, dept_id, create_time, update_time) \
            values (#{username},#{name},#{gender},#{phone},#{job},#{salary},#{image},#{entryDate},#{deptId},#{createTime},#{updateTime})""" )
    int insert(Emp emp);

    /**
     * (批量)删除员工
     */
    int deleteBatch( @Param( "ids" ) List< Integer > ids );

    /**
     * 根据id查询员工
     */
    @Select( "SELECT * FROM emp WHERE id = #{id}" )
    Emp selectById( @Param( "id" ) Integer id );

    /**
     * 修改员工
     */
    int updateById( Emp emp );

    /**
     * 统计员工 职位pos(工作job) - 人数total( count )
     */
    List< JobCountVO > countEmpJobData();

    /**
     * 统计员工 性别name( gender ) - 人数value( count )
     */
    List< OptionVO > countEmpGenderData();

    @Select( """
            SELECT *
            FROM emp
            WHERE dept_id = #{deptId}
            ORDER BY name ASC, id ASC
            """ )
    List< Emp> selectByDeptId( @Param( "deptId" ) Integer deptId );

    @Select( "SELECT * FROM emp WHERE username = #{username} AND password = #{password}" )
    Emp selectByUsernameAndPassword( @Param( "username" ) String username, @Param( "password" ) String password );

    /**
     * 修改员工密码
     */
    @Update( """
            UPDATE emp SET password = #{newPassword} WHERE id = #{empId}
            """ )
    int updatePasswordById( @Param( "empId" ) Integer empId, @Param( "newPassword" ) String newPassword );
}
