package com.awp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 一张截图识别出的多笔交易，批量入库。imageBase64 为该截图压缩图，附到每一笔。
 */
@Data
public class BatchRecordDTO {

    private String imageBase64;

    @NotEmpty(message = "记录不能为空")
    @Valid
    private List<RecordDTO> records;
}
