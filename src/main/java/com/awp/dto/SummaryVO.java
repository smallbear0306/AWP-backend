package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收支汇总：收入、支出、结余。
 */
@Data
public class SummaryVO {
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;
}
