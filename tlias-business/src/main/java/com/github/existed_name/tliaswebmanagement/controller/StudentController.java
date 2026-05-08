package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_pojo.Student;
import com.github.existed_name.tlias_pojo.dto.StudentQueryParam;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.service.StudentService;
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
@RequestMapping( "/students" )
@RestController
public  class StudentController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final StudentService studentService;
    private final EmpService empService;


    // ==================== 构造器 ====================
    @Autowired
    public StudentController( StudentService studentService, EmpService empService ){
        this.studentService = studentService;
        this.empService = empService;
    }


    // ==================== 公有方法 ====================
    /**
     * <pre>
     *     请求数据样例：
     *      /students?name=张三&degree=1&clazzId=2&page=1&pageSize=5
     * </pre>
     */
    @GetMapping
    public Result< PageResult > page( StudentQueryParam queryParam ){
        log.info( "条件分页查询, 查询参数: {}", queryParam );
        PageResult result = studentService.page( queryParam );
        return Result.success( result );
    }

    /**
     * 查询所有学生
     */
    @GetMapping( "/list" )
    public Result< List< Student > > list(){
        log.info( "查询所有学生" );
        List< Student > studentList = studentService.list();
        return Result.success( studentList );
    }

    /**
     * <pre>
     *     添加学生
         请求数据样例：
     {
     "name": "阿大",
     "no": "2024010801",
     "gender": 1,
     "phone": "15909091235",
     "idCard": "159090912351590909",
     "isCollege": 1,
     "address": "昌平回龙观",
     "degree": 4,
     "graduationDate": "2024-01-01",
     "clazzId": 9
     }
     * </pre>
     */
    @LogOperation
    @PostMapping
    public Result< Object > save( @RequestBody Student student ){
        log.info( "添加学生: {}", student );
        studentService.save( student );
        return Result.success();
    }

    /**
     * <pre>
         根据主键ID查询学生的信息
         请求参数样例：
         /students/8
     * </pre>
     */
    @GetMapping( "/{id}" )
    public Result< Student > getById( @PathVariable Integer id ){
        log.info( "查询学生: id = {}", id );
        Student student = studentService.getById( id );
        return Result.success( student );
    }

    /**
     * <pre>
         修改学生的数据信息
         请求数据样例：
     {
     "id": 7,
     "name": "Locos",
     "no": "2023001010",
     "gender": 1,
     "phone": "13712345678",
     "degree": 5,
     "idCard": "110090110090110090",
     "isCollege": 0,
     "address": "回龙观东大街110号",
     "graduationDate": "2020-07-01",
     "violationCount": 0,
     "violationScore": 0,
     "clazzId": 2
     }
     * </pre>
     */
    @LogOperation
    @PutMapping
    public Result< Student > update( @RequestBody Student student ){
        log.info( "更新学生数据: {}", student );
        studentService.update( student );
        return Result.success( student );
    }

    /**
     * <pre>
     *     批量删除学生信息
     *      请求参数样例：
     *      /students/1,2,3
     * </pre>
     */
    @LogOperation
    @DeleteMapping( "/{ids}" )
    public Result< Object > delete( @PathVariable List< Integer > ids ){
        log.info( "删除学生, ids = {}", ids );
        studentService.deleteByIds( ids );
        return Result.success();
    }

    /**
     * <pre>
     > 请求路径：/students/violation/{id}/{score}
     >
     > 请求方式：PUT
     >
     > 接口描述：该接口用于修改学员的数据信息
     * </pre>
     */
    @LogOperation
    @PutMapping( "/violation/{id}/{score}" )
    public Result< Object > recordViolation( @PathVariable( "id" ) Integer id, @PathVariable( "score" ) Integer score ){
        log.info( "违规记录, id = {}, 扣分 = {}", id, score );
        studentService.recordViolation( id, score );
        return Result.success();
    }



    // ==================== 私有辅助方法 ====================

}
