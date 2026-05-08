package com.github.existed_name.tlias_pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLogSummaryVO {
    /** 今日操作总数 */
    private Long todayCount;      //
    /** 累计操作总数 */
    private Long totalCount;      //
    /** 柱状图数据 */
    private List<AvgCostVO> avgCostTimes;    //
    /** 饼图数据 */
    private List<TypeCountVO> totalTypeCounts; //

    @Data
    public static class AvgCostVO {
        private String type; // 添加、删除、修改;
        private Double avgTime;
    }

    @Data
    public static class TypeCountVO {
        private String type; // 添加、删除、修改
        private Integer count;
    }
}