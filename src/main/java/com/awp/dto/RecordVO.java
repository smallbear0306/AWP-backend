package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单展示对象：在账单基础上带上一级/二级分类名称。
 */
@Data
public class RecordVO {
    private Long id;
    private Long accountId;
    private String accountName;
    private Long categoryId;             // 二级分类(叶子) id
    private String categoryName;         // 二级分类名
    private Long parentCategoryId;       // 一级分类 id
    private String parentCategoryName;   // 一级分类名
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private LocalDate recordDate;
    private Integer hasImage;             // 1=有截图
    private LocalDateTime createTime;
}
