package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.Clazz;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.ClazzQueryParam;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ClazzService {
    /**
     * 条件分页查询
     *
     * @param queryParam 查询参数
     */
    PageResult page( @NotNull( message = "班级查询信息不能为空" ) ClazzQueryParam queryParam );

    /**
     * 添加班级
     */
    void save(  @NotNull( message = "班级信息不能为空" ) Clazz clazz );

    /**
     * 根据id查询
     */
    Clazz getById( @NotNull( message = "班级 id 不能为空" ) @Positive( message = "班级 id 必须为正数" ) Integer id );

    /**
     * 更新班级
     */
    void update(  @NotNull( message = "班级信息不能为空" ) Clazz clazz );

    /**
     * 根据id删除班级
     */
    void deleteById( @NotNull( message = "班级 id 不能为空" ) @Positive( message = "班级 id 必须为正数" ) Integer id );

    /**
     * 查询所有班级
     */
    List< Clazz> list();
}
