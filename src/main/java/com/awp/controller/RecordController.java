package com.awp.controller;

import com.awp.common.PageResult;
import com.awp.common.Result;
import com.awp.dto.RecordDTO;
import com.awp.dto.RecordQuery;
import com.awp.dto.RecordVO;
import com.awp.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账单相关接口（需登录）。
 */
@RestController
@RequestMapping("/api/record")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /** 分页查询，过滤条件以 query 参数传入 */
    @GetMapping
    public Result<PageResult<RecordVO>> page(RecordQuery query) {
        return Result.success(recordService.page(query));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody RecordDTO dto) {
        recordService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RecordDTO dto) {
        recordService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return Result.success();
    }
}
