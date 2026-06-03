package com.awp.service;

import com.awp.dto.CategoryDTO;
import com.awp.entity.Category;

import java.util.List;

/**
 * 分类业务接口。
 */
public interface CategoryService {

    List<Category> list(Integer type);

    Category create(CategoryDTO dto);

    void update(Long id, CategoryDTO dto);

    void delete(Long id);
}
