package com.awp.controller;

import com.awp.common.Result;
import com.awp.dto.AccountDTO;
import com.awp.dto.AccountVO;
import com.awp.entity.Account;
import com.awp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户相关接口（需登录）。
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Result<List<AccountVO>> list() {
        return Result.success(accountService.list());
    }

    @PostMapping
    public Result<Account> create(@Valid @RequestBody AccountDTO dto) {
        return Result.success(accountService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AccountDTO dto) {
        accountService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return Result.success();
    }

    /** 划账/更新：直接设置余额 */
    @PutMapping("/{id}/balance")
    public Result<Void> setBalance(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        accountService.setBalance(id, body.get("balance"));
        return Result.success();
    }
}
