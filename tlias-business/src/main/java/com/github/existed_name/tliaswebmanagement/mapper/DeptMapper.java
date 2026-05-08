package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DeptMapper {
    /**
     * 查询所有部门
     */
    @Results( id = "deptMap", value = {
            // 标记 id 然后在其他方法上面引用，就可以不用重复写命名转换
            @Result( property = "createTime", column = "create_time" ),
            // 如果某个 Result 是主键，就补充 id = true
            @Result( property = "updateTime", column = "update_time" )
    } )
    @Select( """
            SELECT *
            FROM dept
            ORDER BY CONVERT( name USING gbk ) ASC , id ASC
            """ )
    List< Dept > selectAll();

    /**
     * 根据id查询部门
     */
    @ResultMap( "deptMap" )
    @Select( """
            SELECT * FROM dept WHERE id = #{id}
            """ )
    Dept selectById( @Param( "id" ) Integer id );

    /**
     * 根据id删除部门
     */
    @ResultMap( "deptMap" )
    @Delete( """
            DELETE FROM dept WHERE id = #{id}
            """ )
    int deleteById( @Param( "id" ) Integer id );

    /**
     * 新增部门
     */
    @Insert( """
            INSERT INTO dept
            ( dept.name, dept.create_time, dept.update_time)
            values( #{name}, #{ createTime }, #{ updateTime } )
            """ )
    int insert(  Dept dept );

    /**
     * 更新部门
     */
    @Update( """
            UPDATE dept
            SET dept.name = #{ name }, dept.update_time = #{ updateTime }
            WHERE id = #{ id }
            """ )
    int updateById( Dept dept );
}
