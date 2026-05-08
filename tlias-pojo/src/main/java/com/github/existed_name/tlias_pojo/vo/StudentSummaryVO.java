package com.github.existed_name.tlias_pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentSummaryVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 学生总数 */
    private Integer totalStudent;
    /** 未分配班级的学生人数 */
    private Integer studentHaveNoClazz;
    /** 班级总数 */
    private Integer totalClazz;
    /** 在读班级数量 */
    private Integer openClazzCount;
    /** 结课班级数量 */
    private Integer endClazzCount;
    /** 未开班级数量 */
    private Integer notStartClazzCount;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
