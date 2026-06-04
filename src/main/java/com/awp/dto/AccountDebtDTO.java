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

    @NotNull(message = "应还本金不能为空")
    private BigDecimal amount;   // 应还本金

    private BigDecimal rate;     // 年利率(%)，可空(=0 不计息)

    private Integer type;        // 0 一次性 / 1 按月还款，默认 0

    private Integer months;      // 期限/期数(月)

    private Integer repayMethod; // 0等额本息/1等额本金/2付息后一次性还本/3一次性还本息

    private Integer status;   // 0 未还款 / 1 已还款 / 2 已逾期，默认 0

    private LocalDate dueDate;

    private String remark;
}
