package com.awp.mapper;

import com.awp.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表数据访问。可见范围 = 系统预设(user_id=0) + 当前用户自定义(user_id=当前)。
 */
public interface CategoryMapper {

    /** 列出当前用户可见的全部分类（预设+自定义），可按类型过滤；按 type、sort_order、id 排序 */
    List<Category> listVisible(@Param("userId") Long userId, @Param("type") Integer type);

    /** 按 id 查询当前用户可见的分类（预设或自有） */
    Category findVisible(@Param("id") Long id, @Param("userId") Long userId);

    /** 按 id 查询当前用户自有的分类（仅 user_id=当前，用于改/删归属校验） */
    Category findOwn(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Category category);

    int update(Category category);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 统计某分类的子分类数（删一级前校验） */
    int countChildren(@Param("parentId") Long parentId);

    /** 统计某分类下当前用户的账单数（删二级前校验） */
    int countRecords(@Param("categoryId") Long categoryId, @Param("userId") Long userId);
}
