package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户截图识别结果：用于预填新建账户/划账。
 */
@Data
public class AccountRecognizeResult {
    private boolean recognized;
    private String message;
    private String name;        // 建议账户名
    private String type;        // 账户类型
    private String bank;        // 银行名(银行卡类)
    private Integer kind;       // 0 储蓄 / 1 信用
    private BigDecimal balance; // 识别到的当前余额
}
