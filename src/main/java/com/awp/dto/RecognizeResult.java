package com.awp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 截图识别结果：用于前端预填"记一笔"表单。
 */
@Data
public class RecognizeResult {
    private boolean recognized;        // AI 是否成功解析
    private String message;            // 失败/提示信息

    private Integer type;              // 0 支出 / 1 收入（由"支出/收入"转换）
    private BigDecimal amount;         // 金额
    private String recordDate;         // YYYY-MM-DD

    private Long categoryId;           // 命中的二级分类 id（命中才填）
    private Long parentCategoryId;     // 对应一级分类 id
    private String categoryL1;         // 模型给的一级名（参考）
    private String categoryL2;         // 模型给的二级名（参考）

    private String remark;             // 建议备注（商家/摘要）

    private String imageBase64;        // 压缩后的 jpg base64（前端预览 + 提交时回传入库）
}
