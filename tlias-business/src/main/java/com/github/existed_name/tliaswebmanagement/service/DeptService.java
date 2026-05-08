package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.Dept;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface DeptService {
    /**
     * 查询所有部门
     */
    List< Dept > findAll();

    /**
     * 根据id查询部门
     */
    Dept findById( @NotNull( message = "部门 id 不能为空" ) @Positive( message = "部门 id 必须为正数" ) Integer id );

    /**
     * 根据id删除部门
     */
    void deleteById( @NotNull( message = "部门 id 不能为空" ) @Positive( message = "部门 id 必须为正数" ) Integer id );

    /**
     * 新增部门
     */
    void add( @NotNull( message = "部门信息不能为空" ) Dept dept );

    /**
     * 编辑部门
     */
    void updateById( @NotNull( message = "部门信息不能为空" ) Dept dept );
}
