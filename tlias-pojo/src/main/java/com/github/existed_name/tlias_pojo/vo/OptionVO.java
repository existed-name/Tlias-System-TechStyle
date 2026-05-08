package com.github.existed_name.tlias_pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 1. 通用统计对象 (适用于性别、学历等饼图)
 * 对应 Echarts 的 {name: 'xxx', value: 123}
 */
@Data
@AllArgsConstructor
public class OptionVO {
    private String name;
    private Long value;
}