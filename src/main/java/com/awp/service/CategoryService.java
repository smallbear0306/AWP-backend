package com.awp.service;

import com.awp.dto.CategoryDTO;
import com.awp.dto.CategoryNode;
import com.awp.entity.Category;

import java.util.List;

/**
 * 分类业务接口。
 */
public interface CategoryService {

    /** 分类树（一级带二级 children），可按类型过滤 */
    List<CategoryNode> tree(Integer type);

    /** 扁平列表（预设+自定义），可按类型过滤 */
    List<Category> list(Integer type);

    /** 新增用户自定义分类（一级或二级） */
    Category create(CategoryDTO dto);

    /** 修改用户自定义分类（不可改预设） */
    void update(Long id, CategoryDTO dto);

    /** 删除用户自定义分类（不可删预设；有子分类或账单时禁止） */
    void delete(Long id);
}
