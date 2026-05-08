package com.github.existed_name.tlias_pojo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ClazzQueryParam {
    private String name;

    /*
        直接接收 LocalDate，
        前端传 2023-01-01 时，Spring 有时会因为格式解析不出来而报错。
        建议在字段上加上 @DateTimeFormat 注解，显式指定格式
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
    
    private Integer page = 1;     // 设置起始页码默认值
    private Integer pageSize = 10; // 设置默认值
}