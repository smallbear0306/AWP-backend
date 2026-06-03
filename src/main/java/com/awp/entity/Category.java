package com.awp.entity;

import lombok.Data;

/**
 * 分类实体，对应 category 表。
 * 三级体系：收支类型 type -> 一级分类(parentId=null) -> 二级分类(parentId 指向一级)。
 */
@Data
public class Category {
    private Long id;
    private Long userId;        // 0=系统预设(全局)，>0=用户自定义
    private Long parentId;      // null=一级分类，否则指向一级分类 id
    private Integer type;       // 0 支出 / 1 收入
    private String name;
    private String description; // 括号说明，作提示
    private String icon;
    private Integer sortOrder;
}
