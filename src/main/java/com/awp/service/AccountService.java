package com.awp.service;

import com.awp.dto.AccountDTO;
import com.awp.dto.AccountVO;
import com.awp.entity.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户业务接口。
 */
public interface AccountService {

    List<AccountVO> list();

    Account create(AccountDTO dto);

    void update(Long id, AccountDTO dto);

    void delete(Long id);

    /** 划账/更新：直接设置余额 */
    void setBalance(Long id, BigDecimal balance);
}
