package com.awp.mapper;

import com.awp.entity.Account;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户数据访问。均按 user_id 隔离。
 */
public interface AccountMapper {

    List<Account> listByUser(@Param("userId") Long userId);

    Account findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Account account);

    /** 修改基本信息（不含余额） */
    int update(Account account);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 余额增量（记账联动）：balance += delta */
    int addBalance(@Param("id") Long id, @Param("userId") Long userId, @Param("delta") BigDecimal delta);

    /** 直接设置余额（划账/平帐） */
    int setBalance(@Param("id") Long id, @Param("userId") Long userId, @Param("balance") BigDecimal balance);

    /** 该账户下的账单数（删除前校验） */
    int countRecords(@Param("accountId") Long accountId);
}
