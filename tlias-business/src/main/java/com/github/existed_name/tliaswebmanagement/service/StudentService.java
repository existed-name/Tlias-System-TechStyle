package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Student;
import com.github.existed_name.tlias_pojo.dto.StudentQueryParam;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface StudentService {
    /**
     * 条件分页查询
     *
     * @param queryParam 查询参数
     */
    PageResult page( @NotNull( message = "学生查询参数不能为空" ) StudentQueryParam queryParam );

    /**
     * 添加学生
     */
    void save( @NotNull( message = "学生信息不能为空" ) Student student );

    /**
     * 根据id查询
     */
    Student getById( @NotNull( message = "学生 id 不能为空" ) @Positive( message = "学生 id 必须为正数" ) Integer id );

    /**
     * 更新学生
     */
    void update( @NotNull( message = "学生信息不能为空" ) Student student );

    /**
     * 根据id删除学生
     */
    void deleteByIds(  @NotEmpty( message = "选中删除的学生不能为空" ) List< Integer > ids );

    /**
     * 查询所有学生
     */
    List< Student> list();

    /**
     * 违规记录
     *
     * @param id 学生id
     * @param score 扣分
     */
    void recordViolation( 
            @NotNull( message = "学生 id 不能为空" ) @Positive( message = "学生 id 必须为正数" ) Integer id,
            @PositiveOrZero( message = "扣分不能为负数" )  Integer score );
}
