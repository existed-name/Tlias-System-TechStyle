package com.github.existed_name.tlias_pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 */
@Data
public final class Result<T> implements Serializable {
    // ==================== 常量 ====================
    /** 结果相关常量类 */
    public static final class ResultConstants {
        /** 成功结果的常量 */
        public static final class Success {
            /** 成功代码 */
            public static final Integer CODE = 1;
            /** 成功 */
            public static final String MSG = "success";

            private Success(){}
        }

        /** 失败结果的常量 */
        public static final class Error {
            /** 失败代码 */
            public static final Integer CODE = 0;

            private Error(){}
        }

        private ResultConstants(){}
    }


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 编码：1成功0失败 */
    private Integer code;
    /** 错误信息 */
    private String msg;
    /** 数据 */
    private T data;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success( T object ){
        Result<T> result = new Result<>();
        result.data = object;
        result.code = ResultConstants.Success.CODE;
        result.msg = ResultConstants.Success.MSG;
        return result;
    }

    public static <T> Result<T> error( String msg ){
        Result<T> result = new Result<>();
        result.code = ResultConstants.Error.CODE;
        result.msg = msg;
        return result;
    }


    // ==================== 私有辅助方法 ====================

}
