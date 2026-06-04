package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户截图识别结果：一张图可能含多个账户（如银行总览页的多张卡/多种余额）。
 */
@Data
public class AccountRecognizeResult {
    private boolean recognized;
    private String message;
    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private String name;        // 建议账户名
        private String type;        // 账户类型
        private String bank;        // 银行名(银行卡类)
        private Integer kind;       // 0 储蓄 / 1 信用
        private BigDecimal balance; // 识别到的余额
    }
}
