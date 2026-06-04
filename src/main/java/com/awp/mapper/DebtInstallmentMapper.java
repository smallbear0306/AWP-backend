package com.awp.mapper;

import com.awp.entity.DebtInstallment;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 负债分期数据访问。
 */
public interface DebtInstallmentMapper {

    int insert(DebtInstallment d);

    List<DebtInstallment> listByDebt(@Param("debtId") Long debtId);

    DebtInstallment findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    int deleteByDebt(@Param("debtId") Long debtId);

    /** 某账户未结清分期合计(未还0 + 逾期2) */
    BigDecimal sumOutstandingByAccount(@Param("accountId") Long accountId, @Param("userId") Long userId);
}
