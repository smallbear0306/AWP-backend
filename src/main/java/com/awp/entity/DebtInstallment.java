package com.awp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 负债分期：一期还款。status：0 未还 / 1 已还 / 2 已逾期。
 */
@Data
public class DebtInstallment {
    private Long id;
    private Long userId;
    private Long accountId;
    private Long debtId;
    private Integer period;
    private LocalDate dueDate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal amount;
    private Integer status;
}
