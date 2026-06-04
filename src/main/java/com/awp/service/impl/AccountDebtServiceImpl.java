package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.AccountDebtDTO;
import com.awp.dto.DebtVO;
import com.awp.entity.AccountDebt;
import com.awp.entity.DebtInstallment;
import com.awp.mapper.AccountDebtMapper;
import com.awp.mapper.AccountMapper;
import com.awp.mapper.DebtInstallmentMapper;
import com.awp.service.AccountDebtService;
import com.awp.util.DebtCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户负债业务实现。负债头 + N 条分期；存额按未结清分期合计。
 */
@Service
public class AccountDebtServiceImpl implements AccountDebtService {

    private final AccountDebtMapper debtMapper;
    private final DebtInstallmentMapper installmentMapper;
    private final AccountMapper accountMapper;

    public AccountDebtServiceImpl(AccountDebtMapper debtMapper, DebtInstallmentMapper installmentMapper,
                                  AccountMapper accountMapper) {
        this.debtMapper = debtMapper;
        this.installmentMapper = installmentMapper;
        this.accountMapper = accountMapper;
    }

    @Override
    public List<DebtVO> listByAccount(Long accountId) {
        Long userId = UserContext.getUserId();
        validateAccount(accountId, userId);
        List<DebtVO> out = new ArrayList<>();
        for (AccountDebt d : debtMapper.listByAccount(accountId, userId)) {
            out.add(toVO(d, installmentMapper.listByDebt(d.getId())));
        }
        return out;
    }

    @Override
    @Transactional
    public AccountDebt create(AccountDebtDTO dto) {
        Long userId = UserContext.getUserId();
        validateAccount(dto.getAccountId(), userId);
        AccountDebt d = fill(new AccountDebt(), dto);
        d.setUserId(userId);
        d.setAccountId(dto.getAccountId());
        d.setStatus(0);
        debtMapper.insert(d);
        genInstallments(d);
        return d;
    }

    @Override
    @Transactional
    public void update(Long id, AccountDebtDTO dto) {
        Long userId = UserContext.getUserId();
        AccountDebt exist = debtMapper.findByIdAndUser(id, userId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        AccountDebt d = fill(exist, dto);
        debtMapper.update(d);
        // 财务参数可能变，重算分期（重置还款状态）
        installmentMapper.deleteByDebt(id);
        genInstallments(d);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        if (debtMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        installmentMapper.deleteByDebt(id);
        debtMapper.deleteByIdAndUser(id, userId);
    }

    @Override
    public List<DebtInstallment> installments(Long debtId) {
        Long userId = UserContext.getUserId();
        if (debtMapper.findByIdAndUser(debtId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return installmentMapper.listByDebt(debtId);
    }

    @Override
    public void setInstallmentStatus(Long installmentId, Integer status) {
        Long userId = UserContext.getUserId();
        if (installmentMapper.findByIdAndUser(installmentId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        int s = status == null ? 0 : status;
        installmentMapper.updateStatus(installmentId, userId, s);
    }

    /** 依据头信息生成分期并入库 */
    private void genInstallments(AccountDebt d) {
        double principal = d.getAmount() == null ? 0 : d.getAmount().doubleValue();
        double rate = d.getRate() == null ? 0 : d.getRate().doubleValue();
        int months = (d.getType() != null && d.getType() == 1)
                ? (d.getMonths() == null ? 1 : d.getMonths())
                : (d.getMonths() == null ? 1 : d.getMonths()); // 一次性也用 months 作期限算息
        int method = d.getRepayMethod() == null ? 3 : d.getRepayMethod();
        List<DebtInstallment> items = DebtCalculator.generate(principal, rate, months, method, d.getDueDate());
        for (DebtInstallment it : items) {
            it.setUserId(d.getUserId());
            it.setAccountId(d.getAccountId());
            it.setDebtId(d.getId());
            installmentMapper.insert(it);
        }
    }

    private AccountDebt fill(AccountDebt d, AccountDebtDTO dto) {
        d.setName(dto.getName());
        d.setAmount(dto.getAmount());
        d.setRate(dto.getRate() == null ? BigDecimal.ZERO : dto.getRate());
        d.setType(dto.getType() == null ? 0 : dto.getType());
        d.setMonths(dto.getMonths());
        // 一次性只能一次性还本息(3)；按月默认等额本息(0)
        Integer m = dto.getRepayMethod();
        if (d.getType() == 0) {
            m = 3;
        } else if (m == null || m == 3) {
            m = 0;
        }
        d.setRepayMethod(m);
        d.setDueDate(dto.getDueDate());
        d.setRemark(dto.getRemark());
        return d;
    }

    private DebtVO toVO(AccountDebt d, List<DebtInstallment> inst) {
        DebtVO vo = new DebtVO();
        vo.setId(d.getId());
        vo.setName(d.getName());
        vo.setType(d.getType());
        vo.setRepayMethod(d.getRepayMethod());
        vo.setRate(d.getRate());
        vo.setMonths(d.getMonths());
        vo.setPrincipal(d.getAmount());
        vo.setDueDate(d.getDueDate());
        vo.setRemark(d.getRemark());

        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        int paid = 0;
        for (DebtInstallment it : inst) {
            interest = interest.add(nz(it.getInterest()));
            total = total.add(nz(it.getAmount()));
            if (it.getStatus() != null && it.getStatus() == 1) {
                paid++;
            } else {
                outstanding = outstanding.add(nz(it.getAmount()));
            }
        }
        vo.setInterestTotal(interest);
        vo.setTotal(total);
        vo.setOutstanding(outstanding);
        vo.setPeriods(inst.size());
        vo.setPaidPeriods(paid);
        return vo;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private void validateAccount(Long accountId, Long userId) {
        if (accountId == null || accountMapper.findByIdAndUser(accountId, userId) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "账户不存在");
        }
    }
}
