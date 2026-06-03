package com.awp.mapper;

import com.awp.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表数据访问。所有操作均按 user_id 隔离。
 */
public interface CategoryMapper {

    /** 列出某用户的全部分类（可按类型过滤，type 为 null 则不过滤） */
    List<Category> listByUser(@Param("userId") Long userId, @Param("type") Integer type);

    /** 按 id + userId 查询（用于归属校验） */
    Category findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Category category);

    int update(Category category);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 统计该分类下的账单数（删除前校验） */
    int countRecords(@Param("categoryId") Long categoryId, @Param("userId") Long userId);
}
