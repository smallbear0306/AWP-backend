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
    private BigDecimal amount;
    private Integer type;
    private Integer months;     // 按月还款的贷款月数
    private Integer status;
    private LocalDate dueDate;
    private String remark;
    private LocalDateTime createTime;
}
