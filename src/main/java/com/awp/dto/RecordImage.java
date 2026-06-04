package com.awp.dto;

import lombok.Data;

/**
 * 账单截图。
 */
@Data
public class RecordImage {
    private Long recordId;
    private byte[] content;
    private String contentType;
}
