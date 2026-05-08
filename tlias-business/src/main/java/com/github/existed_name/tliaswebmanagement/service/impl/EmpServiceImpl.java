package com.github.existed_name.tliaswebmanagement.service.impl;

import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.EmpExpr;
import com.github.existed_name.tlias_pojo.LoginInfo;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.EmpQueryParam;
import com.github.existed_name.tlias_util.JwtUtils;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import com.github.existed_name.tliaswebmanagement.mapper.EmpExprMapper;
import com.github.existed_name.tliaswebmanagement.mapper.EmpMapper;
import com.github.existed_name.tliaswebmanagement.service.EmpService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional( readOnly = true ) // 类级别: 默认所有方法都是只读事务,可以优化大查询性能
public class EmpServiceImpl implements EmpService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final EmpExprMapper empExprMapper;
    private final EmpMapper empMapper;


    // ==================== 构造器 ====================
    @Autowired
    public EmpServiceImpl( EmpMapper empMapper, EmpExprMapper empExprMapper ){
        this.empMapper = empMapper;
        this.empExprMapper = empExprMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public List< Emp > list(){
        List< Emp > empList = empMapper.selectAll();
        return empList;
    }


    @Override
    public PageResult page( EmpQueryParam queryParam ){
        PageHelper.startPage( queryParam.getPage(), queryParam.getPageSize() );

        List< Emp > empList = empMapper.selectByQuery( queryParam );
        Page< Emp > empPage = ( Page< Emp> ) empList;

        return new PageResult( empPage.getTotal(), empPage.getResult() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void save( Emp emp ){
//1.补全基础属性
        emp.setCreateTime( LocalDateTime.now() );
        emp.setUpdateTime( LocalDateTime.now() );

        //2.保存员工基本信息
        empMapper.insert( emp );

        //3. 保存员工的工作经历信息 - 批量
        Integer empId = emp.getId();
        List< EmpExpr > exprList = emp.getExprList();
        if(!CollectionUtils.isEmpty(exprList)){
            exprList.forEach(empExpr -> empExpr.setEmpId(empId));
            empExprMapper.insertBatch(exprList);
        }
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIds( List< Integer > ids ){
        // 去重 id
        ids = ids.stream().distinct().toList();
        empExprMapper.deleteBatchByEmpIds( ids );
        int affectedRows = empMapper.deleteBatch( ids );
        if ( affectedRows != ids.size() ){
            throw new BusinessException( "部分员工: 不存在 | 删除失败" );
        }
    }

    @Override
    public Emp getInfo( Integer id ){
        Emp emp = empMapper.selectById( id );
        List< EmpExpr > empExprList = empExprMapper.findByEmpId( id );
        emp.setExprList( empExprList );
        return emp;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( Emp emp ){
        //1. 根据ID更新员工基本信息
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.updateById(emp);

        //2. 根据员工ID删除员工的工作经历信息 【删除老的】
        empExprMapper.deleteBatchByEmpIds( Arrays.asList( emp.getId() ) );

        //3. 新增员工的工作经历数据 【新增新的】
        Integer empId = emp.getId();
        List<EmpExpr> exprList = emp.getExprList();
        if(!CollectionUtils.isEmpty(exprList)){
            exprList.forEach(empExpr -> empExpr.setEmpId(empId));
            empExprMapper.insertBatch(exprList);
        }
    }

    @Override
    public LoginInfo login( Emp emp ){
        Emp empLogin = empMapper.selectByUsernameAndPassword( emp.getUsername(), emp.getPassword() );
        if ( empLogin == null ){
            return null;
        }

        //1. 生成JWT令牌
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("id", empLogin.getId());
        dataMap.put("username", empLogin.getUsername());

        String jwt = JwtUtils.generateJwt( dataMap );
        return new LoginInfo( empLogin.getId(), empLogin.getUsername(), empLogin.getName(), jwt );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updatePassword( Integer empId, String oldPassword, String newPassword ){
        Emp emp = empMapper.selectById( empId );
        if ( emp == null ){
            throw new BusinessException( "该员工不存在" );
        }

        if ( ! emp.getPassword().equals( oldPassword ) ){
            throw new BusinessException( "原密码错误" );
        }

        empMapper.updatePasswordById( empId, newPassword );
    }


    // ==================== 私有辅助方法 ====================

}
