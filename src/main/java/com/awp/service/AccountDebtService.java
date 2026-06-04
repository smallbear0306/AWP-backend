package com.awp.service;

import com.awp.dto.AccountDebtDTO;
import com.awp.entity.AccountDebt;

import java.util.List;

/**
 * 账户负债业务接口。
 */
public interface AccountDebtService {

    List<AccountDebt> listByAccount(Long accountId);

    AccountDebt create(AccountDebtDTO dto);

    void update(Long id, AccountDebtDTO dto);

    void delete(Long id);
}
