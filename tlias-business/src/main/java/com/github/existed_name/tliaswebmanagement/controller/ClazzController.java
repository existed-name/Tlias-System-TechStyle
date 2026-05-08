package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Clazz;
import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_pojo.dto.ClazzQueryParam;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.service.ClazzService;
import com.github.existed_name.tliaswebmanagement.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping( "/clazzs" ) // http://localhost:8080/clazzs
@RestController
public  class ClazzController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final ClazzService clazzService;
    private final EmpService empService;


    // ==================== 构造器 ====================
    @Autowired
    public ClazzController( ClazzService clazzService, EmpService empService ){
        this.clazzService = clazzService;
        this.empService = empService;
    }


    // ==================== 公有方法 ====================
    /**
     * <pre>
     *     请求数据样例：
     *      /clazzs?name=java&begin=2023-01-01&end=2023-06-30&page=1&pageSize=5
     * </pre>
     */
    @GetMapping
    public Result< PageResult > page( ClazzQueryParam queryParam ){
        log.info( "条件分页查询, 查询参数: {}", queryParam );
        PageResult result = clazzService.page( queryParam );
        return Result.success( result );
    }

    /**
     * 查询所有班级
     * 请求路径：/clazzs/list
     */
    @GetMapping( "/list" )
    public Result< List< Clazz > > list(){
        log.info( "查询所有班级" );
        List< Clazz > clazzList = clazzService.list();
        return Result.success( clazzList );
    }

    /**
     * 查询所有班主任(所有员工)
     */
    @GetMapping( "/listHeadTeachers" )
    public Result< List< Emp > > listHeadTeachers(){
        log.info( "查询所有班主任(所有员工)" );
        List< Emp > empList = empService.list();
        return Result.success( empList );
    }

    /**
     * <pre>
     *     添加班级
         请求数据样例：
         {
         "name": "JavaEE就业166期",
         "room": "101",
         "beginDate": "2023-06-01",
         "endDate": "2024-01-25",
         "masterId": 7,
         "subject": 1
         }
     * </pre>
     */
    @LogOperation
    @PostMapping
    public Result< Object > save( @RequestBody Clazz clazz ){
        log.info( "添加班级: {}", clazz );
        clazzService.save( clazz );
        return Result.success();
    }

    /**
     * <pre>
         根据主键ID查询班级的信息
         请求参数样例：
         /clazzs/8
     * </pre>
     */
    @GetMapping( "/{id}" )
    public Result< Clazz > getById( @PathVariable Integer id ){
        log.info( "查询班级: id = {}", id );
        Clazz clazz = clazzService.getById( id );
        return Result.success( clazz );
    }

    /**
     * <pre>
         修改班级的数据信息
         请求数据样例：
         {
         "id": 8,
         "name": "JavaEE就业166期",
         "room": "101",
         "beginDate": "2023-06-01",
         "endDate": "2024-01-25",
         "masterId": 7,
         "subject": 1
         }
     * </pre>
     */
    @LogOperation
    @PutMapping
    public Result< Clazz > update( @RequestBody Clazz clazz ){
        log.info( "更新班级数据: {}", clazz );
        clazzService.update( clazz );
        return Result.success( clazz );
    }

    /**
     * <pre>
     *     删除班级信息
     *      请求参数样例：
     *      /clazzs/5
     * </pre>
     */
    @LogOperation
    @DeleteMapping( "/{id}" )
    public Result< Object > delete( @PathVariable Integer id ){
        log.info( "删除班级, id = {}", id );
        clazzService.deleteById( id );
        return Result.success();
    }



    // ==================== 私有辅助方法 ====================

}
