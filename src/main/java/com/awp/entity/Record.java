package com.awp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单流水实体，对应 record 表。type：0 支出 / 1 收入。
 */
@Data
public class Record {
    private Long id;
    private Long userId;
    private Long categoryId;
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
