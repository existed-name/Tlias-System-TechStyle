package com.github.existed_name.tliaswebmanagement.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.existed_name.tlias_pojo.Emp;
import com.github.existed_name.tlias_pojo.LoginLog;
import com.github.existed_name.tlias_pojo.LoginInfo;
import com.github.existed_name.tlias_pojo.OperateLog;
import com.github.existed_name.tlias_pojo.Result;
import com.github.existed_name.tlias_util.CurrentHolder;
import com.github.existed_name.tliaswebmanagement.annotation.LogOperation;
import com.github.existed_name.tliaswebmanagement.mapper.LoginLogMapper;
import com.github.existed_name.tliaswebmanagement.mapper.OperateLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
//@Order( 2 )
public class RecordLogAspect {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OperateLogMapper operateLogMapper;
    private final ObjectMapper jsonMapper;
    private final LoginLogMapper loginLogMapper;


    // ==================== 构造器 ====================

    public RecordLogAspect( LoginLogMapper loginLogMapper, ObjectMapper jsonMapper, OperateLogMapper operateLogMapper ){
        this.loginLogMapper = loginLogMapper;
        this.jsonMapper = jsonMapper;
        this.operateLogMapper = operateLogMapper;
    }


    // ==================== 公有方法 ====================
    @Around("@annotation(logOperation)")
    public Object recordOperateLog(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long startTime = System.currentTimeMillis();   // 直接在这里记录开始时间
        Object result = null;
        Throwable exception = null;                    // 新增：用于区分成功/失败，作为备用returnValue

        try {
            result = joinPoint.proceed();              // 执行目标方法（上传文件等）
            return result;
        } catch (Throwable e) {
            exception = e;                             // 记录异常，但不吞掉它
            throw e;                                   // 必须重新抛出！让全局异常处理器继续工作
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 构建日志对象
            OperateLog operateLog = new OperateLog();  // 最好在 finally 里新建，避免复用

            operateLog.setOperateEmpId(getOperatorId());
            operateLog.setOperateTime(LocalDateTime.now());
            operateLog.setClassName(joinPoint.getTarget().getClass().getName());
            operateLog.setMethodName(joinPoint.getSignature().getName());
            operateLog.setCostTime(costTime);

            // 方法参数（上传文件时建议特殊处理，避免大文件序列化失败）
            try {
                operateLog.setMethodParams(jsonMapper.writeValueAsString(joinPoint.getArgs()));
            } catch (Exception e) {
                operateLog.setMethodParams("[参数序列化失败]");
            }

            // 返回值 / 异常信息
            if (exception != null) {
                operateLog.setReturnValue("异常: " + exception.getClass().getSimpleName()
                        + " - " + exception.getMessage());
                // 如果你的 OperateLog 有 status 或 errorMsg 字段，建议单独设置
                // operateLog.setStatus(0); // 0=失败
            } else {
                try {
                    operateLog.setReturnValue(jsonMapper.writeValueAsString(result));
                } catch (Exception e) {
                    operateLog.setReturnValue("[返回值序列化失败]");
                }
            }

            try {
                // 插入日志
                operateLogMapper.insert(operateLog);
            } catch (Exception e) {
                RecordLogAspect.log.error("记录操作日志失败", e);
            }
        }
    }


    @Around("execution(* com.github.existed_name.tliaswebmanagement.controller.LoginController.login*(..))")
    public Object recordLoginLog( ProceedingJoinPoint joinPoint) throws Throwable {
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginTime(LocalDateTime.now());

        // 尝试从参数中获取用户名（预防性编程）
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof Emp) {
            Emp emp = (Emp) args[0];
            loginLog.setUsername(emp.getUsername());
            // 注意：生产环境通常不记录明文密码哦！
            loginLog.setPassword(emp.getPassword());
        }

        long startTime = System.currentTimeMillis();
        Object resultObj;

        try {
            resultObj = joinPoint.proceed();

            // 成功执行逻辑
            loginLog.setCostTime(System.currentTimeMillis() - startTime);

            if (resultObj instanceof Result) {
                Result<?> result = (Result<?>) resultObj;
                boolean success = (result.getCode() == 1);
                loginLog.setIsSuccess(success ? (short) 1 : (short) 0);

                // 只有成功且 Data 不为空时获取 Token
                if (success && result.getData() instanceof LoginInfo) {
                    loginLog.setJwt(((LoginInfo) result.getData()).getToken());
                }
            }
        } catch (Throwable e) {
            // 捕获异常，标记登录失败
            loginLog.setCostTime(System.currentTimeMillis() - startTime);
            loginLog.setIsSuccess((short) 0);
            throw e; // 记得把异常抛出去，不然 Controller 接收不到
        } finally {
            // 无论成功失败，只要逻辑走到了这里就记录
            try {
                loginLogMapper.insert( loginLog );
            } catch (Exception e) {
                log.error( "登录日志记录异常", e );
            }
        }

        return resultObj;
    }


    // ==================== 私有辅助方法 ====================
    private Integer getOperatorId(){
        return CurrentHolder.getCurrentId();
    }

}
