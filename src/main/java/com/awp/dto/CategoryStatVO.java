package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类统计：某分类下的金额合计（用于饼图占比）。
 */
@Data
public class CategoryStatVO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
}
