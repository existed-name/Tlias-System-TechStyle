package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_pojo.dto.EmpQueryParam;
import com.github.existed_name.tlias_pojo.dto.PasswordDto;
import com.github.existed_name.tlias_util.CurrentHolder;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping( "/emps" ) // http://localhost:8080/emps
@RestController
public  class EmpController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private EmpService empService;


    // ==================== 构造器 ====================
    @Autowired
    public EmpController( EmpService empService ){
        this.empService = empService;
    }


    // ==================== 公有方法 ====================

    /**
     * 查询所有员工
     */
    @GetMapping( "/list" )
    public Result< List< Emp > > list( ){
        log.info( "查询所有员工" );
        List< Emp > empList = empService.list();
        return Result.success( empList );
    }

    /**
     * 分页查询员工
     */
    @GetMapping // http://localhost:8080/emps?name=&gender=1&begin=2010-01-01&end=2026-01-01&pageStart=1&pageSize=10
    public Result< PageResult > page( EmpQueryParam queryParam ){
        log.info( "分页查询, 查询参数 {}", queryParam );
        PageResult pageResult = empService.page( queryParam );
        return Result.success( pageResult );
    }

    /**
     * <pre>
     *     根据ID查询
     *     请求参数样例：
     * /emps/1
     * </pre>
     */
    @GetMapping( "/{id}" )
    public Result< Emp > getInfo( @PathVariable Integer id ){
        log.info( "查询员工: id = {}", id );
        Emp emp = empService.getInfo( id );
        return Result.success( emp );
    }

    /**
     * 添加员工
     */
    @LogOperation
    @PostMapping
    public Result< Emp > save( @RequestBody Emp emp ){
        log.info( "添加员工: {}", emp );
        empService.save( emp );
        return Result.success( emp );
    }

    /**
     * <pre>
     * (批量)删除员工
     *
     * 请求参数样例：
     * /emps?ids=1,2,3
     * </pre>
     */
    @LogOperation
    @DeleteMapping
    public Result< Object > delete( @RequestParam( "ids" ) List< Integer > ids ){
        log.info( "删除员工: ids = {}", ids );
        empService.deleteByIds( ids );
        return Result.success();
    }

    /**
     * <pre>
     *     修改员工信息
     * </pre>
     */
    @LogOperation
    @PutMapping
    public Result< Emp > update( @RequestBody Emp emp ){
        log.info( "修改员工信息: {}", emp );
        empService.update( emp );
        return Result.success( emp );
    }

    /**
     * 修改密码
     */
    @LogOperation
    @PutMapping( "/password" )
    public Result< Object > updatePassword( @RequestBody PasswordDto passwordDto ){
        log.info( "修改员工密码, 请求数据: {}", passwordDto );
        Integer empId = CurrentHolder.getCurrentId();
        String oldPassword = passwordDto.getOldPassword();
        String newPassword = passwordDto.getNewPassword();

        empService.updatePassword( empId, oldPassword, newPassword );
        return Result.success();
    }

    // ==================== 私有辅助方法 ====================

}
