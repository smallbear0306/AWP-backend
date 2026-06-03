package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单展示对象：在账单基础上带上分类名称。
 */
@Data
public class RecordVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
