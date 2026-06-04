package com.awp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 负债新增/修改参数。
 */
@Data
public class AccountDebtDTO {

    @NotNull(message = "请选择账户")
    private Long accountId;

    private String name;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private Integer type;     // 0 一次性 / 1 按月还款，默认 0

    private Integer status;   // 0 未还款 / 1 已还款 / 2 已逾期，默认 0

    private LocalDate dueDate;

    private String remark;
}
