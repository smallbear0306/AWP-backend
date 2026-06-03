package com.awp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类新增/修改参数（仅用于用户自定义分类）。
 * parentId 为空 = 新增一级分类；parentId 指向某一级 = 新增其下二级分类。
 */
@Data
public class CategoryDTO {

    private Long parentId; // null=一级；否则=该一级下的二级

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "类型不能为空")
    private Integer type; // 0 支出 / 1 收入

    private String description;

    private String icon;
}
