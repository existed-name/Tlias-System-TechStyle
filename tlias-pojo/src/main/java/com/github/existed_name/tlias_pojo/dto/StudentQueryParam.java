package com.github.existed_name.tlias_pojo.dto;

import lombok.Data;

@Data
public class StudentQueryParam {
    private String name;
    private Integer degree;
    private Integer clazzId;
    private Integer page = 1;     // 设置起始页码默认值
    private Integer pageSize = 10; // 设置默认值
}