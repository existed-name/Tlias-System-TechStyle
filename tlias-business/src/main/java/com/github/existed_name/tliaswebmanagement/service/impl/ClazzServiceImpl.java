package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.Clazz;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.Student;
import com.github.existed_name.tlias_pojo.dto.ClazzQueryParam;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import com.github.existed_name.tliaswebmanagement.mapper.ClazzMapper;
import com.github.existed_name.tliaswebmanagement.mapper.StudentMapper;
import com.github.existed_name.tliaswebmanagement.service.ClazzService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class ClazzServiceImpl implements ClazzService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final ClazzMapper clazzMapper;
    private final StudentMapper studentMapper;


    // ==================== 构造器 ====================
    @Autowired
    public ClazzServiceImpl( ClazzMapper clazzMapper, StudentMapper studentMapper ){
        this.clazzMapper = clazzMapper;
        this.studentMapper = studentMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public PageResult page( ClazzQueryParam queryParam ){
        PageHelper.startPage( queryParam.getPage(), queryParam.getPageSize() );

        List< Clazz > clazzList = clazzMapper.selectByQuery( queryParam );
        Page< Clazz > page = ( Page< Clazz > ) clazzList;

        return new PageResult( page.getTotal(), page.getResult() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void save( Clazz clazz ){
        // 新增对象的时候不能对 id 判空，因为这个时候数据库还不存在该对象 id，只有修改时才拿得到 id、判空
        clazz.setCreateTime( LocalDateTime.now() );
        clazz.setUpdateTime( LocalDateTime.now() );
        clazzMapper.insert( clazz ); // 遇到相同名称的班级, 直接异常抛给全局异常处理器
    }

    @Override
    public Clazz getById( Integer id ){
        return clazzMapper.selectById( id );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( Clazz clazz ){
        validateClazzId( clazz.getId() );
        validateClazzName( clazz.getName() );

        clazz.setUpdateTime( LocalDateTime.now() );
        int affectedRow = clazzMapper.updateById( clazz );
        if ( affectedRow <= 0 ){
            throw new BusinessException( "该班级不存在 || 数据没有发生改变" );
        }
    }


    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteById( Integer id ){
        List< Student > students = studentMapper.selectByClazzId( id );
        if ( students != null && ! students.isEmpty() ){
            throw new BusinessException( "该班级下有学生, 不能直接删除" );
        }

        int affectedRow = clazzMapper.deleteById( id );
        if ( affectedRow <= 0 ){
            throw new BusinessException( "该班级不存在" );
        }
    }

    @Override
    public List< Clazz > list(){
        return clazzMapper.selectAll();
    }


    // ==================== 私有辅助方法 ====================
    private void validateClazzName( String clazzName ){
        if ( clazzName == null || clazzName.trim().isBlank() ){
            throw new BusinessException( "班级名称不能为空" );
        }
    }

    private void validateClazzId( Integer clazzId ){
        if ( clazzId == null || clazzId <= 0 ){
            throw new BusinessException( "班级 id 不能为空或者非正数" );
        }
    }

}
