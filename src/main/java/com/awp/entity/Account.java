package com.awp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体。余额由记账增减/划账维护；存额=余额-未结清负债(见负债表)。
 */
@Data
public class Account {
    private Long id;
    private Long userId;
    private String name;
    private String type;        // 储蓄卡/信用卡/支付宝余额/微信余额/花呗/余额宝/零钱通/理财/饭卡/现金/其他
    private String bank;        // 银行名(银行卡类)
    private Integer kind;       // 0 储蓄 / 1 信用
    private BigDecimal balance;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
