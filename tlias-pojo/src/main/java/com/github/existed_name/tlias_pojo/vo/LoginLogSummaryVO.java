package com.github.existed_name.tlias_pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogSummaryVO {
    /** 今日登录人数 */
    private Long todayLoginCount;
    /** 今日登录失败次数 */
    private Long todayFailCount;
    /** 饼图数据 */
    private List<PieChartVO> totalSuccessFail;
    /** 柱状图数据 */
    private List<FailRankVO> failRank;

    @Data
    @AllArgsConstructor
    public static class PieChartVO {
        private String name;  // "成功" 或 "失败"
        private Integer value;
    }

    @Data
    public static class FailRankVO {
        private String username;
        private Integer count;
    }
}