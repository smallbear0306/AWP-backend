package com.awp.mapper;

import com.awp.entity.AccountDebt;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户负债数据访问。均按 user_id 隔离。
 */
public interface AccountDebtMapper {

    List<AccountDebt> listByAccount(@Param("accountId") Long accountId, @Param("userId") Long userId);

    AccountDebt findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insert(AccountDebt debt);

    int update(AccountDebt debt);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 某账户未结清负债合计(未还款0 + 已逾期2) */
    BigDecimal sumOutstandingByAccount(@Param("accountId") Long accountId, @Param("userId") Long userId);
}
