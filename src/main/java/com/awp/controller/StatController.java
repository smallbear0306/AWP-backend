package com.awp.controller;

import com.awp.common.Result;
import com.awp.dto.CategoryStatVO;
import com.awp.dto.SummaryVO;
import com.awp.dto.TrendVO;
import com.awp.service.StatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计相关接口（需登录）。日期范围参数可选。
 */
@RestController
@RequestMapping("/api/stat")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    /** 收支汇总 */
    @GetMapping("/summary")
    public Result<SummaryVO> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statService.summary(startDate, endDate));
    }

    /** 分类占比（type：0 支出 / 1 收入，默认支出） */
    @GetMapping("/category")
    public Result<List<CategoryStatVO>> categoryStat(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statService.categoryStat(type, startDate, endDate));
    }

    /** 收支趋势（按月） */
    @GetMapping("/trend")
    public Result<List<TrendVO>> trend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statService.trend(startDate, endDate));
    }
}
