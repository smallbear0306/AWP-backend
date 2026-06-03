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

    int insert(Category category);

    /** 按 id 更新（名称/说明/图标） */
    int updateById(Category category);

    /** 按 id 删除单个分类 */
    int deleteById(@Param("id") Long id);

    /** 删除某一级下的全部二级分类 */
    int deleteChildren(@Param("parentId") Long parentId);

    /** 统计某二级分类下的账单数（全部用户，删除前校验避免孤儿） */
    int countRecordsByLeaf(@Param("categoryId") Long categoryId);

    /** 统计某一级下全部二级分类的账单数（全部用户） */
    int countRecordsByParent(@Param("parentId") Long parentId);
}
