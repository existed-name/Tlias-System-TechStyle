package com.github.existed_name.tliaswebmanagement.exception;

import com.github.existed_name.tlias_pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（@RestControllerAdvice 适用于 REST 接口）
 * <p>
 * 作用：统一捕获并处理控制器层抛出的各种异常，返回前端友好的 Result 格式。
 * 优点：避免用户看到丑陋的异常堆栈，提升用户体验，同时便于后端日志排查。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     *
     * 适用于服务层主动抛出的业务逻辑错误（如参数校验失败、权限不足、数据不存在等）。
     * 返回状态码：400 Bad Request（客户端请求有问题）
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException( BusinessException e) {
        log.warn("业务异常: {}", e.getMessage(), e);  // warn 级别，业务异常不需要 error 堆栈
        return Result.error(e.getMessage());
    }

    /**
     * 处理所有未被上面方法捕获的系统异常（兜底处理）
     *
     * 适用于未知的运行时异常、NullPointerException、系统错误等。
     * 返回状态码：500 Internal Server Error
     * 注意：生产环境不要把 e.getMessage() 直接返回给前端，防止泄露敏感信息。
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        // 对用户友好，不暴露具体错误细节
        return Result.error("系统异常，请稍后重试或联系管理员");
    }

    /**
     * 处理所有数据库相关异常（MyBatis + Spring Data）
     *
     * 包括：
     * - MySQL 服务未启动、已关闭、连接被拒绝
     * - 数据库操作失败（如查询、插入等）
     *
     * 统一返回友好提示，不再区分具体类型（简单可靠）
     */
    @ExceptionHandler({DataAccessException.class, MyBatisSystemException.class})
    public Result<String> handleDataAccessException(Exception e) {
        log.error("数据库访问异常（MySQL 可能未启动或连接失败）", e);

        // 统一友好提示
        return Result.error("数据库服务暂不可用，可能未启动或出现异常");
    }

    /**
     * 处理数据库唯一约束冲突（DuplicateKeyException）
     * <pre>
     * 适用所有表的 UNIQUE 字段：
     * - dept.name
     * - emp.username
     * - emp.phone
     * - clazz.name
     * - student.no
     * - student.phone
     * - student.id_card
     * <pre>
     * 优点：
     * 1. 所有 unique 字段只需在这里统一处理，Service 层再也不用写一堆 selectByXXX 检查
     * 2. 并发安全（数据库自己保证）
     * 3. 代码简洁，后续再加 10 个 unique 字段也不用改 Service
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一约束冲突（DuplicateKeyException）", e);

        // 1. 获取完整的错误信息（包含 cause 的 SQL 信息）
        String detail = e.getMessage();
        if (e.getCause() != null) {
            detail += " | " + e.getCause().getMessage();
        }
        String lowerDetail = detail.toLowerCase();

        // 2. 默认提示
        String errorMessage = "数据重复，请检查输入信息";

        // 3. 根据表.字段精准匹配，返回对应中文提示
        if (lowerDetail.contains("dept.name")) {
            errorMessage = "该部门名称已被其他部门使用";
        } else if (lowerDetail.contains("emp.username")) {
            errorMessage = "该用户名已被其他员工使用";
        } else if (lowerDetail.contains("emp.phone")) {
            errorMessage = "该手机号已被其他员工使用";
        } else if (lowerDetail.contains("clazz.name")) {
            errorMessage = "该班级名称已被其他班级使用";
        } else if (lowerDetail.contains("student.no")) {
            errorMessage = "该学号已被其他学员使用";
        } else if (lowerDetail.contains("student.phone")) {
            errorMessage = "该手机号已被其他学员使用";
        } else if (lowerDetail.contains("student.id_card")) {
            errorMessage = "该身份证号已被其他学员使用";
        }

        // 4. 返回友好提示
        return Result.error(errorMessage);
    }

    // ====================== 工具方法 ======================


}