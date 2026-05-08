package com.github.existed_name.tlias_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日志ID */
    private Integer id;

    /** 操作人ID */
    private Integer operateEmpId;

    /** 操作时间 */
    private LocalDateTime operateTime;

    /** 操作类(全类名) */
    private String className;

    /** 操作方法 */
    private String methodName;

    /** 方法参数(JSON) */
    private String methodParams;

    /** 返回值(JSON) */
    private String returnValue;

    /** 执行耗时(ms) */
    private Long costTime;

    /** 进行操作的员工的姓名 */
    private String operateEmpName;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
