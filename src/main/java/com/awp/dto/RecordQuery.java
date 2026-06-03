package com.awp.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 账单分页查询参数（过滤条件均可选）。
 */
@Data
public class RecordQuery {
    private Integer page = 1;
    private Integer size = 10;
    private Integer type;          // 0 支出 / 1 收入
    private Long categoryId;
    private LocalDate startDate;    // 起始日期（含）
    private LocalDate endDate;      // 结束日期（含）

    /** 计算 SQL 偏移量 */
    public int getOffset() {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        return (p - 1) * s;
    }

    public int getLimit() {
        return (size == null || size < 1) ? 10 : size;
    }
}
