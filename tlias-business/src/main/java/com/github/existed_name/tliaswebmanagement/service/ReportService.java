package com.github.existed_name.tliaswebmanagement.service;

import com.github.existed_name.tlias_pojo.vo.ClazzStudentCountVO;
import com.github.existed_name.tlias_pojo.vo.JobCountVO;
import com.github.existed_name.tlias_pojo.vo.OptionVO;
import com.github.existed_name.tlias_pojo.vo.StudentSummaryVO;

import java.util.List;

public interface ReportService {
    /**
     * 统计各个职位的员工人数
     *
     * @return 包含职位pos链表、人数total链表的包装对象
     */
    List< JobCountVO > getEmpJobData();

    /**
     * 统计各个职位的员工人数
     *
     * @return 包含职位pos链表、人数total链表的包装对象
     */
    List< OptionVO > getEmpGenderData();

    /**
     * 统计各个班级人数
     */
    List< ClazzStudentCountVO > getStudentCountData();

    /**
     * 统计所有学员的学历信息
     */
    List< OptionVO > getStudentDegreeData();

    /**
     * 统计学员综合数据 - 学生总人数、未分配班级学生人数、在读班级数量、所有班级数量
     */
    StudentSummaryVO getStudentSummaryData();
}