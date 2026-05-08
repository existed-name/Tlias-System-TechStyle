package com.github.existed_name.tliaswebmanagement.mapper;

import com.github.existed_name.tlias_pojo.Student;
import com.github.existed_name.tlias_pojo.dto.StudentQueryParam;
import com.github.existed_name.tlias_pojo.vo.ClazzStudentCountVO;
import com.github.existed_name.tlias_pojo.vo.OptionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface StudentMapper {
    /**
     * 条件查询
     */
    List< Student > selectByQuery( StudentQueryParam queryParam );

    /**
     * 添加学生
     */
    @Insert( "INSERT INTO student " +
            " ( name, no, gender, phone, id_card, is_college, address, degree, graduation_date, clazz_id, violation_count, violation_score, create_time, update_time )  " +
            " VALUES ( #{name}, #{no}, #{gender}, #{phone}, #{idCard}, #{isCollege}, #{address}, #{degree}, #{graduationDate}, #{clazzId}, #{violationCount}, #{violationScore}, #{createTime}, #{updateTime} )" )
    int insert( Student student );

    /**
     * 根据id查询
     */
    @Select( """
            SELECT
                s.id, s.name, s.no, s.gender, s.phone, s.id_card, s.is_college, s.address, s.degree, s.graduation_date, s.clazz_id, s.violation_count, s.violation_score, s.create_time, s.update_time,
                c.name AS clazz_name
            FROM student AS s
            LEFT JOIN clazz AS c
            ON s.clazz_id = c.id
            WHERE s.id = #{id}
            """ )
    Student selectById( @Param( "id" ) Integer id );

    /**
     * 更新学生信息
     */
    int update( Student student );

    /**
     * 根据id批量删除
     */
    int deleteByIds( @Param( "ids" ) List< Integer > ids );

    /**
     * 查询所有学生
     */
    @Select( """
            SELECT
                s.id, s.name, s.no, s.gender, s.phone, s.id_card, s.is_college, s.address, s.degree, s.graduation_date, s.clazz_id, s.violation_count, s.violation_score, s.create_time, s.update_time,
                c.name AS clazz_name
            FROM student AS s
            LEFT JOIN clazz AS c
            ON s.clazz_id = c.id 
            ORDER BY CONVERT( c.name USING gbk ) ASC, CONVERT( s.name USING gbk ) ASC, s.id ASC
            """ )
    List< Student > selectAll();

    /**
     * 更新违规记录
     */
    @Update( "UPDATE student " +
            "SET violation_count = #{violationCount}, violation_score = #{violationScore}, update_time = #{updateTime} " +
            "WHERE id = #{id}" )
    int updateViolation( Student student );

    /**
     * 根据班级id查询对应学生
     */
    @Select( """
            SELECT id, name, no, gender, phone, id_card, is_college, address, degree, graduation_date, clazz_id, violation_count, violation_score, create_time, update_time 
            FROM student WHERE clazz_id = #{clazzId} 
            ORDER BY name ASC, id ASC
        """ )
    List< Student > selectByClazzId( @Param( "clazzId" ) Integer clazzId );

    /**
     * 统计各个班级的学生人数
     */
    List< ClazzStudentCountVO > countStudentCountData();

    /**
     * 统计所有学员的学历信息
     */
    List< OptionVO > countStudentDegreeData();

    /**
     * 学员总数
     */
    @Select( "SELECT count( 0 ) FROM student" )
    Integer countAll();

    /**
     * 没有分配班级的学生人数
     */
    @Select( """
            SELECT count( 0 )
            FROM student AS s LEFT JOIN clazz AS c
            ON s.clazz_id = c.id
            WHERE c.name IS NULL
            """ )
    Integer countStudentHaveNoClazz();
}
