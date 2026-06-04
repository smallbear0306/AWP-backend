package com.awp.service;

import com.awp.dto.AccountDebtDTO;
import com.awp.dto.DebtVO;
import com.awp.entity.AccountDebt;
import com.awp.entity.DebtInstallment;

import java.util.List;

/**
 * 账户负债业务接口。负债 = 头信息(本金/利率/还款方式/期限) + N 条分期。
 */
public interface AccountDebtService {

    List<DebtVO> listByAccount(Long accountId);

    AccountDebt create(AccountDebtDTO dto);

    void update(Long id, AccountDebtDTO dto);

    void delete(Long id);

    /** 某负债的分期明细 */
    List<DebtInstallment> installments(Long debtId);

    /** 标记某一期状态 0未还/1已还/2逾期 */
    void setInstallmentStatus(Long installmentId, Integer status);
}
