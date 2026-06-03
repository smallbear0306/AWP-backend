package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.CategoryDTO;
import com.awp.entity.Category;
import com.awp.mapper.CategoryMapper;
import com.awp.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现。所有操作绑定当前登录用户。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.listByUser(UserContext.getUserId(), type);
    }

    @Override
    public Category create(CategoryDTO dto) {
        Category category = new Category();
        category.setUserId(UserContext.getUserId());
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(dto.getIcon());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void update(Long id, CategoryDTO dto) {
        Long userId = UserContext.getUserId();
        if (categoryMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Category category = new Category();
        category.setId(id);
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(dto.getIcon());
        categoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        if (categoryMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 该分类下仍有账单时禁止删除
        if (categoryMapper.countRecords(id, userId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在账单，无法删除");
        }
        categoryMapper.deleteByIdAndUser(id, userId);
    }
}
