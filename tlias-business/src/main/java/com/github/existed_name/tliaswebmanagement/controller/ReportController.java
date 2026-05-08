package com.github.existed_name.tliaswebmanagement.controller;

import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_pojo.vo.ClazzStudentCountVO;
import com.github.existed_name.tlias_pojo.vo.JobCountVO;
import com.github.existed_name.tlias_pojo.vo.OptionVO;
import com.github.existed_name.tlias_pojo.vo.StudentSummaryVO;
import com.github.existed_name.tliaswebmanagement.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/report")
@RestController
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController( ReportService reportService ){
        this.reportService = reportService;
    }

    /**
     * 统计各个职位的员工人数 - 柱状图
     */
    @GetMapping("/empJobData")  // http://localhost:8080/report/empJobData
    public Result< List< JobCountVO > > getEmpJobData(){
        log.info("统计各个职位的员工人数");
        List< JobCountVO > empJobData = reportService.getEmpJobData();
        return Result.success( empJobData );
    }

    /**
     * 统计各个性别的员工人数 - 饼图
     */
    @GetMapping("/empGenderData")  // http://localhost:8080/report/empGenderData
    public Result< List< OptionVO > > getEmpGenderData(){
        log.info("统计各个性别的员工人数");
        List< OptionVO > empGenderData = reportService.getEmpGenderData();
        return Result.success( empGenderData );
    }

    /**
     * 统计各个班级人数 - 柱状图
     */
    @GetMapping( "/studentCountData" )
    public Result< List< ClazzStudentCountVO > > getStudentCountData(){
        log.info("统计各个班级人数");
        List< ClazzStudentCountVO > clazzStudentCount = reportService.getStudentCountData();
        return Result.success( clazzStudentCount );
    }

    /**
     * 统计所有学员的学历信息 - 饼图
     */
    @GetMapping( "/studentDegreeData" )
    public  Result< List< OptionVO > > getStudentDegreeData(){
        log.info( "统计所有学员的学历信息" );
        List< OptionVO > studentDegreeData = reportService.getStudentDegreeData();
        return Result.success( studentDegreeData );
    }

    /**
     * 学员综合数据 - 学生总人数、未分配班级学生人数、在读班级数量、所有班级数量
     */
    @GetMapping( "/studentSummaryData" )
    public Result< StudentSummaryVO > getStudentSummaryData(){
        log.info( "统计学员综合数据: 学生总人数、未分配班级学生人数、在读班级数量、所有班级数量" );
        StudentSummaryVO studentSummaryData = reportService.getStudentSummaryData();
        return Result.success( studentSummaryData );
    }
}