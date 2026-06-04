package com.awp.controller;

import com.awp.common.Result;
import com.awp.dto.AccountDebtDTO;
import com.awp.entity.AccountDebt;
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

    /** 列出某账户的负债 */
    @GetMapping
    public Result<List<AccountDebt>> list(@RequestParam Long accountId) {
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
}
