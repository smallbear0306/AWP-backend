package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.AccountDebtDTO;
import com.awp.entity.AccountDebt;
import com.awp.mapper.AccountDebtMapper;
import com.awp.mapper.AccountMapper;
import com.awp.service.AccountDebtService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账户负债业务实现。负债不直接改账户余额，仅在"存额=余额-未结清负债"中体现。
 */
@Service
public class AccountDebtServiceImpl implements AccountDebtService {

    private final AccountDebtMapper debtMapper;
    private final AccountMapper accountMapper;

    public AccountDebtServiceImpl(AccountDebtMapper debtMapper, AccountMapper accountMapper) {
        this.debtMapper = debtMapper;
        this.accountMapper = accountMapper;
    }

    @Override
    public List<AccountDebt> listByAccount(Long accountId) {
        Long userId = UserContext.getUserId();
        validateAccount(accountId, userId);
        return debtMapper.listByAccount(accountId, userId);
    }

    @Override
    public AccountDebt create(AccountDebtDTO dto) {
        Long userId = UserContext.getUserId();
        validateAccount(dto.getAccountId(), userId);
        AccountDebt d = new AccountDebt();
        d.setUserId(userId);
        d.setAccountId(dto.getAccountId());
        d.setName(dto.getName());
        d.setAmount(dto.getAmount());
        d.setType(dto.getType() == null ? 0 : dto.getType());
        d.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        d.setDueDate(dto.getDueDate());
        d.setRemark(dto.getRemark());
        debtMapper.insert(d);
        return d;
    }

    @Override
    public void update(Long id, AccountDebtDTO dto) {
        Long userId = UserContext.getUserId();
        AccountDebt d = debtMapper.findByIdAndUser(id, userId);
        if (d == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        d.setName(dto.getName());
        d.setAmount(dto.getAmount());
        d.setType(dto.getType() == null ? d.getType() : dto.getType());
        d.setStatus(dto.getStatus() == null ? d.getStatus() : dto.getStatus());
        d.setDueDate(dto.getDueDate());
        d.setRemark(dto.getRemark());
        debtMapper.update(d);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        if (debtMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        debtMapper.deleteByIdAndUser(id, userId);
    }

    private void validateAccount(Long accountId, Long userId) {
        if (accountId == null || accountMapper.findByIdAndUser(accountId, userId) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "账户不存在");
        }
    }
}
