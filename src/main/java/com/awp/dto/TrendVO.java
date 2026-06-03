package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 趋势统计：按月的收入与支出（用于折线图）。
 */
@Data
public class TrendVO {
    private String month; // 形如 2026-06
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
}
