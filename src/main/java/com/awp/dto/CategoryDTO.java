package com.awp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类新增/修改参数。
 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "类型不能为空")
    private Integer type; // 0 支出 / 1 收入

    private String icon;
}
