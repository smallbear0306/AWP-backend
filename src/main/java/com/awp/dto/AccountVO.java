package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户展示对象：余额 + 负债合计 + 存额(=余额-未结清负债)。
 */
@Data
public class AccountVO {
    private Long id;
    private String name;
    private String type;
    private String bank;
    private Integer kind;
    private BigDecimal balance;
    private BigDecimal debtTotal;   // 未结清负债合计(未还款+已逾期)
    private BigDecimal netAmount;   // 存额 = balance - debtTotal
    private String icon;
    private LocalDateTime updateTime;
}
