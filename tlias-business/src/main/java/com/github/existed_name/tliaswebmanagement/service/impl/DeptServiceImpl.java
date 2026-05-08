package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.Dept;
import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import com.github.existed_name.tliaswebmanagement.mapper.DeptMapper;
import com.github.existed_name.tliaswebmanagement.mapper.EmpMapper;
import com.github.existed_name.tliaswebmanagement.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class DeptServiceImpl implements DeptService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DeptMapper deptMapper;
    private final EmpMapper empMapper;


    // ==================== 构造器 ====================
    @Autowired
    public DeptServiceImpl( DeptMapper deptMapper, EmpMapper empMapper ){
        this.deptMapper = deptMapper;
        this.empMapper = empMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public List< Dept > findAll(){
        return deptMapper.selectAll();
    }

    @Override
    public Dept findById( Integer id ){
        return deptMapper.selectById( id );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteById( Integer id ){
        List< Emp > empList = empMapper.selectByDeptId( id );
        if ( empList != null && ! empList.isEmpty() ){
            throw new BusinessException( "该部门存在员工, 不能直接删除" );
        }

        int affectedRow = deptMapper.deleteById( id );
        if ( affectedRow <= 0 ){
            throw new BusinessException( "该部门不存在" );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void add( Dept dept ){
        dept.setCreateTime( LocalDateTime.now() );
        dept.setUpdateTime( LocalDateTime.now() );
        deptMapper.insert( dept );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateById( Dept dept ){
        validateDeptId( dept.getId() );
        dept.setUpdateTime( LocalDateTime.now() );
        deptMapper.updateById( dept );
    }


    // ==================== 私有辅助方法 ====================
    private void validateDeptId( Integer deptId ){
        if ( deptId == null || deptId <= 0 ){
            throw new BusinessException( "部门 id 不能为空或者非正数" );
        }
    }


}
