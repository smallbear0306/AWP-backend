package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.CategoryDTO;
import com.awp.dto.CategoryNode;
import com.awp.entity.Category;
import com.awp.mapper.CategoryMapper;
import com.awp.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类业务实现。
 * 可见范围 = 系统预设(user_id=0) + 当前用户自定义；预设不可改/删。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryNode> tree(Integer type) {
        List<Category> all = categoryMapper.listVisible(UserContext.getUserId(), type);
        // 一级节点（保持查询顺序）
        Map<Long, CategoryNode> roots = new LinkedHashMap<>();
        List<Category> children = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId() == null) {
                roots.put(c.getId(), toNode(c));
            } else {
                children.add(c);
            }
        }
        // 挂二级
        for (Category c : children) {
            CategoryNode parent = roots.get(c.getParentId());
            if (parent != null) {
                parent.getChildren().add(toNode(c));
            }
        }
        return new ArrayList<>(roots.values());
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.listVisible(UserContext.getUserId(), type);
    }

    @Override
    public Category create(CategoryDTO dto) {
        Long userId = UserContext.getUserId();
        Category category = new Category();
        category.setUserId(userId);
        category.setType(dto.getType());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setSortOrder(0);

        if (dto.getParentId() != null) {
            // 新增二级：父必须可见，且类型一致，且父本身是一级
            Category parent = categoryMapper.findVisible(dto.getParentId(), userId);
            if (parent == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "父分类不存在");
            }
            if (parent.getParentId() != null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只能在一级分类下新增二级分类");
            }
            if (!parent.getType().equals(dto.getType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "二级分类收支类型需与一级一致");
            }
            category.setParentId(dto.getParentId());
        } else {
            category.setParentId(null);
        }

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void update(Long id, CategoryDTO dto) {
        Long userId = UserContext.getUserId();
        requireOwn(id, userId);
        Category category = new Category();
        category.setId(id);
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        categoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        Category own = requireOwn(id, userId);
        if (own.getParentId() == null && categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该一级分类下存在二级分类，无法删除");
        }
        if (categoryMapper.countRecords(id, userId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在账单，无法删除");
        }
        categoryMapper.deleteByIdAndUser(id, userId);
    }

    /** 校验该分类属于当前用户（预设 user_id=0 不属于任何用户，会校验失败） */
    private Category requireOwn(Long id, Long userId) {
        Category own = categoryMapper.findOwn(id, userId);
        if (own == null) {
            // 预设或不存在
            Category visible = categoryMapper.findVisible(id, userId);
            if (visible != null && visible.getUserId() == 0L) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "系统预设分类不可修改或删除");
            }
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return own;
    }

    private CategoryNode toNode(Category c) {
        CategoryNode n = new CategoryNode();
        n.setId(c.getId());
        n.setParentId(c.getParentId());
        n.setType(c.getType());
        n.setName(c.getName());
        n.setDescription(c.getDescription());
        n.setIcon(c.getIcon());
        n.setSystem(c.getUserId() != null && c.getUserId() == 0L);
        return n;
    }
}
