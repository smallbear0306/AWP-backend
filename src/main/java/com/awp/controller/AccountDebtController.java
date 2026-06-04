package com.awp.controller;

import com.awp.common.Result;
import com.awp.dto.AccountDebtDTO;
import com.awp.dto.DebtVO;
import com.awp.entity.AccountDebt;
import com.awp.entity.DebtInstallment;
import com.awp.service.AccountDebtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 账户负债接口（需登录）。
 */
@RestController
@RequestMapping("/api/debt")
public class AccountDebtController {

    private final AccountDebtService debtService;

    public AccountDebtController(AccountDebtService debtService) {
        this.debtService = debtService;
    }

    /** 列出某账户的负债(含本金/利息/总额/未结清/期数) */
    @GetMapping
    public Result<List<DebtVO>> list(@RequestParam Long accountId) {
        return Result.success(debtService.listByAccount(accountId));
    }

    @PostMapping
    public Result<AccountDebt> create(@Valid @RequestBody AccountDebtDTO dto) {
        return Result.success(debtService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AccountDebtDTO dto) {
        debtService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        debtService.delete(id);
        return Result.success();
    }

    /** 某负债的分期明细 */
    @GetMapping("/{id}/installments")
    public Result<List<DebtInstallment>> installments(@PathVariable Long id) {
        return Result.success(debtService.installments(id));
    }

    /** 标记某一期还款状态 0未还/1已还/2逾期 */
    @PutMapping("/installment/{id}")
    public Result<Void> setInstallment(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        debtService.setInstallmentStatus(id, body.get("status"));
        return Result.success();
    }
}
