package com.awp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账户负债。type：0 一次性 / 1 按月还款；status：0 未还款 / 1 已还款 / 2 已逾期。
 */
@Data
public class AccountDebt {
    private Long id;
    private Long userId;
    private Long accountId;
    private String name;
    private BigDecimal amount;       // 应还本金
    private BigDecimal rate;         // 年利率(%)
    private Integer type;
    private Integer months;         // 期限/期数(月)
    private Integer repayMethod;    // 0等额本息/1等额本金/2付息后一次性还本/3一次性还本息
    private Integer status;
    private LocalDate dueDate;
    private String remark;
    private LocalDateTime createTime;
}
