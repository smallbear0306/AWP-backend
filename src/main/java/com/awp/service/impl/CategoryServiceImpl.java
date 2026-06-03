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
        requireVisible(id, userId);
        Category category = new Category();
        category.setId(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        Category cat = requireVisible(id, userId);
        if (cat.getParentId() == null) {
            // 一级：连同其下二级一并删除；若子树下有账单则禁止
            if (categoryMapper.countRecordsByParent(id) > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该一级分类下存在账单，请先处理账单再删除");
            }
            categoryMapper.deleteChildren(id);
            categoryMapper.deleteById(id);
        } else {
            // 二级：有账单则禁止
            if (categoryMapper.countRecordsByLeaf(id) > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在账单，请先处理账单再删除");
            }
            categoryMapper.deleteById(id);
        }
    }

    /** 校验该分类对当前用户可见（预设或自有）；不存在则报错 */
    private Category requireVisible(Long id, Long userId) {
        Category cat = categoryMapper.findVisible(id, userId);
        if (cat == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return cat;
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
