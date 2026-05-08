package com.github.existed_name.tlias_pojo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class LoginLogQueryParam {
    private Integer page = 1;     // 设置起始页码默认值
    private Integer pageSize = 10; // 设置默认值
    private String username;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}