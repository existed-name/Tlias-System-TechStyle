package com.github.existed_name.tliaswebmanagement.service;


import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.LoginInfo;
import com.github.existed_name.tlias_pojo.PageResult;
import com.github.existed_name.tlias_pojo.dto.EmpQueryParam;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmpService {
    /**
     * 查询所有员工
     */
    List< Emp > list();

    /**
     * 条件分页查询
     *
     * @param queryParam 查询参数
     */
    PageResult page( @NotNull( message = "员工查询信息不能为空" ) EmpQueryParam queryParam );

    /**
     * 保存员工
     */
    void save( @NotNull( message = "员工信息不能为空" ) Emp emp );

    /**
     * 删除员工
     */
    void deleteByIds( @NotEmpty( message = "选中删除的员工不能为空" ) List< Integer > ids );

    /**
     * 根据id查询员工
     */
    Emp getInfo( @NotNull( message = "员工 id 不能为空" ) @Positive( message = "员工 id 必须为正数" ) Integer id );

    /**
     * 更新员工信息
     */
    void update( @NotNull( message = "员工信息不能为空" ) Emp emp );

    /**
     * 根据用户输入的信息(用户名、密码)进行登录
     */
    LoginInfo login( @NotNull( message = "员工信息不能为空" ) Emp emp );

    /**
     * 修改员工密码
     */
    void updatePassword(
            @NotNull( message = "员工 id 不能为空" ) @Positive( message = "员工 id 必须为正数" ) Integer empId, 
            @NotEmpty( message = "原始密码不能为空" ) String oldPassword,
            @NotEmpty( message = "修改后的密码不能为空" ) String newPassword );
}
