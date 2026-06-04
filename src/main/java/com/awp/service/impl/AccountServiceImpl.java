package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.AccountDTO;
import com.awp.dto.AccountVO;
import com.awp.entity.Account;
import com.awp.mapper.AccountMapper;
import com.awp.mapper.DebtInstallmentMapper;
import com.awp.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户业务实现。所有操作绑定当前登录用户。
 * 存额 = 余额 - 未结清负债(未还款+已逾期)；步骤一暂无负债表，debtTotal=0。
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final DebtInstallmentMapper installmentMapper;

    public AccountServiceImpl(AccountMapper accountMapper, DebtInstallmentMapper installmentMapper) {
        this.accountMapper = accountMapper;
        this.installmentMapper = installmentMapper;
    }

    @Override
    public List<AccountVO> list() {
        Long userId = UserContext.getUserId();
        List<AccountVO> out = new ArrayList<>();
        for (Account a : accountMapper.listByUser(userId)) {
            BigDecimal debt = installmentMapper.sumOutstandingByAccount(a.getId(), userId);
            out.add(toVO(a, debt == null ? BigDecimal.ZERO : debt));
        }
        return out;
    }

    @Override
    public Account create(AccountDTO dto) {
        Account a = new Account();
        a.setUserId(UserContext.getUserId());
        a.setName(dto.getName());
        a.setType(dto.getType());
        a.setBank(dto.getBank());
        a.setKind(dto.getKind() == null ? 0 : dto.getKind());
        a.setBalance(dto.getBalance() == null ? BigDecimal.ZERO : dto.getBalance());
        a.setIcon(dto.getIcon());
        a.setSortOrder(0);
        accountMapper.insert(a);
        return a;
    }

    @Override
    @Transactional
    public void createBatch(List<AccountDTO> dtos) {
        for (AccountDTO dto : dtos) {
            create(dto);
        }
    }

    @Override
    public void update(Long id, AccountDTO dto) {
        Long userId = UserContext.getUserId();
        Account a = requireOwn(id, userId);
        a.setName(dto.getName());
        a.setType(dto.getType());
        a.setBank(dto.getBank());
        a.setKind(dto.getKind() == null ? a.getKind() : dto.getKind());
        a.setIcon(dto.getIcon());
        accountMapper.update(a);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        requireOwn(id, userId);
        if (accountMapper.countRecords(id) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该账户下存在账单，无法删除");
        }
        accountMapper.deleteByIdAndUser(id, userId);
    }

    @Override
    public void setBalance(Long id, BigDecimal balance) {
        Long userId = UserContext.getUserId();
        requireOwn(id, userId);
        if (balance == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "余额不能为空");
        }
        accountMapper.setBalance(id, userId, balance);
    }

    private Account requireOwn(Long id, Long userId) {
        Account a = accountMapper.findByIdAndUser(id, userId);
        if (a == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return a;
    }

    private AccountVO toVO(Account a, BigDecimal debtTotal) {
        AccountVO vo = new AccountVO();
        vo.setId(a.getId());
        vo.setName(a.getName());
        vo.setType(a.getType());
        vo.setBank(a.getBank());
        vo.setKind(a.getKind());
        vo.setBalance(a.getBalance());
        vo.setDebtTotal(debtTotal);
        vo.setNetAmount(a.getBalance().subtract(debtTotal));
        vo.setIcon(a.getIcon());
        vo.setUpdateTime(a.getUpdateTime());
        return vo;
    }
}
