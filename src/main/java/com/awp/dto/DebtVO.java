package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 负债展示对象：含本金/利息/总额(本息)/未结清/期数进度。
 */
@Data
public class DebtVO {
    private Long id;
    private String name;
    private Integer type;            // 0 一次性 / 1 按月还款
    private Integer repayMethod;
    private BigDecimal rate;
    private Integer months;
    private BigDecimal principal;    // 应还本金(=amount)
    private BigDecimal interestTotal;// 应付利息合计
    private BigDecimal total;        // 总额 = 本金 + 利息
    private BigDecimal outstanding;  // 未结清(未还+逾期)金额
    private Integer periods;         // 总期数
    private Integer paidPeriods;     // 已还期数
    private LocalDate dueDate;
    private String remark;
}
