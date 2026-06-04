package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 识别出的单笔交易（一张截图可能含多笔，如个税=收入一笔+个税支出一笔）。
 */
@Data
public class RecognizeItem {
    private Integer type;              // 0 支出 / 1 收入
    private BigDecimal amount;
    private String recordDate;         // YYYY-MM-DD
    private Long categoryId;           // 命中的二级分类 id
    private Long parentCategoryId;
    private String categoryL1;
    private String categoryL2;
    private String remark;
}
