package com.awp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单新增/修改参数。
 */
@Data
public class RecordDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    private Integer type; // 0 支出 / 1 收入

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    private String remark;

    @NotNull(message = "记账日期不能为空")
    private LocalDate recordDate;
}
