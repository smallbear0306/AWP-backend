package com.awp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量创建账户（一张截图识别出的多个账户，复核后一起创建）。
 */
@Data
public class AccountBatchDTO {

    @NotEmpty(message = "账户不能为空")
    @Valid
    private List<AccountDTO> accounts;
}
