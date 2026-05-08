package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Student;
import com.github.existed_name.tlias_pojo.dto.StudentQueryParam;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import com.github.existed_name.tliaswebmanagement.mapper.StudentMapper;
import com.github.existed_name.tliaswebmanagement.service.StudentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public  class StudentServiceImpl implements StudentService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final StudentMapper studentMapper;


    // ==================== 构造器 ====================
    @Autowired
    public StudentServiceImpl( StudentMapper studentMapper ){
        this.studentMapper = studentMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public PageResult page( StudentQueryParam queryParam ){
        PageHelper.startPage( queryParam.getPage(), queryParam.getPageSize() );

        List< Student > studentList = studentMapper.selectByQuery( queryParam );
        Page< Student > page = ( Page< Student> ) studentList;

        return new PageResult( page.getTotal(), page.getResult() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void save( Student student ){
        student.setCreateTime( LocalDateTime.now() );
        student.setUpdateTime( LocalDateTime.now() );
        studentMapper.insert( student );
    }

    @Override
    public Student getById( Integer id ){
        return studentMapper.selectById( id );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( Student student ){
        student.setUpdateTime( LocalDateTime.now() );
        studentMapper.update( student );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIds( List< Integer > ids ){
        // 去重
        ids = ids.stream().distinct().toList();
        int affectedRows = studentMapper.deleteByIds( ids );
        if ( affectedRows != ids.size() ){
            throw new BusinessException( "部分学生: 不存在 | 删除失败" );
        }
    }

    @Override
    public List< Student > list(){
        return studentMapper.selectAll();
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void recordViolation( Integer id, Integer score ){
        Student student = studentMapper.selectById( id );
        if ( student == null ){
            throw new BusinessException( "该学生不存在" );
        }

        Short violationCount = student.getViolationCount();
        student.setViolationCount( ++violationCount );
        Short violationScore = student.getViolationScore();
        student.setViolationScore( ( short ) (violationScore + score) );

        student.setUpdateTime( LocalDateTime.now() );
        studentMapper.updateViolation( student );
    }


    // ==================== 私有辅助方法 ====================

}
