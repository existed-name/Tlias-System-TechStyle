package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Dept;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理控制器
 */
@Slf4j
@RequestMapping( "/depts" )
@RestController
//@Scope( "prototype" ) // 非单例, 每次创建对象
public  class DeptController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DeptService deptService;


    // ==================== 构造器 ====================
    @Autowired
    public DeptController( DeptService deptService ){
        this.deptService = deptService;
//        log.warn( "DeptController 的构造器" );
    }


    // ==================== 公有方法 ====================
    // -- 增 --
    @LogOperation
    @PostMapping
    public Result< Object > add( @RequestBody Dept dept ){
        log.info( "添加部门: {}", dept );
        deptService.add( dept );
        return Result.success(  );
    }

    // -- 删 --
    @LogOperation
    @DeleteMapping
    public Result< Object > delete( @RequestParam( "id" ) Integer id ){
        log.info( "删除部门, id 为 {}", id );
        deptService.deleteById( id );
        return Result.success(  );
    }

    // -- 改 --
    @LogOperation
    @PutMapping // ?id=1
    public Result< Dept > update( @RequestBody Dept dept ){
        log.info( "更新部门: {}", dept );
        deptService.updateById( dept );
        return Result.success( dept );
    }

    // -- 查 --
    @GetMapping
//            ( "/list" )
    public Result< List< Dept > > list(){
        log.info( "展示所有部门" );
        List< Dept > depts = deptService.findAll();
        return Result.success( depts );
    }

    @GetMapping( "/{id}" )
    public Result< Dept > getById( @PathVariable( "id" ) Integer id ){
        log.info( "查询 id 为 {} 的部门", id );
        Dept dept = deptService.findById( id );
        return Result.success( dept );
    }



    // ==================== 私有辅助方法 ====================

}
