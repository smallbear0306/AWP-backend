package com.awp.mapper;

import com.awp.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表数据访问。
 */
public interface UserMapper {

    /** 按用户名查询（用于登录与重名校验） */
    User findByUsername(@Param("username") String username);

    /** 按主键查询 */
    User findById(@Param("id") Long id);

    /** 新增用户，回填自增主键 */
    int insert(User user);
}
