package com.awp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点（一级带 children 二级）。system=true 表示系统预设，前端据此禁用编辑/删除。
 */
@Data
public class CategoryNode {
    private Long id;
    private Long parentId;
    private Integer type;
    private String name;
    private String description;
    private String icon;
    private boolean system;            // userId == 0
    private List<CategoryNode> children = new ArrayList<>();
}
