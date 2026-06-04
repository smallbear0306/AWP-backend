package com.awp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户新增/修改参数。
 */
@Data
public class AccountDTO {

    @NotBlank(message = "账户名不能为空")
    private String name;

    @NotBlank(message = "账户类型不能为空")
    private String type;

    private String bank;

    private Integer kind;        // 0 储蓄 / 1 信用，默认 0

    private String icon;

    /** 新建时的初始余额（划账用单独接口） */
    private BigDecimal balance;
}
