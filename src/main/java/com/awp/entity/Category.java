package com.awp.entity;

import lombok.Data;

/**
 * 分类实体，对应 category 表。type：0 支出 / 1 收入。
 */
@Data
public class Category {
    private Long id;
    private Long userId;
    private String name;
    private Integer type;
    private String icon;
}
