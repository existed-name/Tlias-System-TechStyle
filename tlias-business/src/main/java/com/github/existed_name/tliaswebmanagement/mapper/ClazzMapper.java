package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.Clazz;
import com.github.existed_name.tlias_pojo.dto.ClazzQueryParam;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClazzMapper {
    /**
     * 条件查询
     */
    List< Clazz > selectByQuery( ClazzQueryParam queryParam );

    /**
     * 添加班级
     */
    @Insert( """
            INSERT INTO clazz
            ( name, room, begin_date, end_date, master_id, subject, create_time, update_time )
            VALUES ( #{name}, #{room}, #{beginDate}, #{endDate}, #{masterId}, #{subject}, #{createTime}, #{updateTime} )""" )
    int insert( Clazz clazz );

    /**
     * 根据id查询班级
     */
    Clazz selectById( @Param( "id" ) Integer id );

    /**
     * 更新班级信息
     */
    int updateById( Clazz clazz );

    /**
     * 根据id删除
     */
    @Delete( "DELETE FROM clazz WHERE id = #{id}" )
    int deleteById( @Param( "id" ) Integer id );

    /**
     * 查询所有班级
     */
    List< Clazz> selectAll();

    /**
     * 根据名称查询
     */
    @Select( "SELECT id, name, room, begin_date, end_date, master_id, subject, create_time, update_time FROM clazz WHERE name = #{name}" )
    Clazz selectByName( @Param( "name" ) String name );

    /**
     * 班级总数
     */
    @Select( "SELECT count( 0 ) FROM clazz" )
    Integer countAll();

    /**
     * 在读班级总数
     */
    @Select( """
            SELECT count( 0 )
            FROM clazz
            WHERE begin_date <= now() AND now() <= end_date
            """ )
    Integer countOpenClazz();

    /**
     * 已结课班级数量
     */
    @Select( """
            SELECT count( 0 )
            FROM clazz
            WHERE end_date < now()
            """  )
    Integer countEndClazz();

    /**
     * 未开课班级数量
     */
    @Select( """
            SELECT count( 0 )
            FROM clazz
            WHERE begin_date > now()
            """  )
    Integer countNotStart();

}
