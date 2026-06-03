package com.awp.controller;

import com.awp.common.Result;
import com.awp.dto.CategoryDTO;
import com.awp.dto.CategoryNode;
import com.awp.entity.Category;
import com.awp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类相关接口（需登录）。
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 分类树（一级带二级 children），可按 type 过滤 */
    @GetMapping("/tree")
    public Result<List<CategoryNode>> tree(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.tree(type));
    }

    /** 分类扁平列表（预设+自定义），可按 type 过滤 */
    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.list(type));
    }

    @PostMapping
    public Result<Category> create(@Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
