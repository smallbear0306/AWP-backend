package com.awp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 截图识别结果：可含多笔交易，供前端复核后批量入库。
 */
@Data
public class RecognizeResult {
    private boolean recognized;            // AI 是否成功解析
    private String message;                // 失败/提示信息
    private String imageBase64;            // 压缩后的 jpg base64（预览 + 入库回传）
    private List<RecognizeItem> items = new ArrayList<>();
}
