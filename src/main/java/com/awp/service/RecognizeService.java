package com.awp.service;

import com.awp.dto.RecognizeResult;

/**
 * 截图识别业务。
 */
public interface RecognizeService {

    /** 压缩图片 → 调 AI 识别 → 返回预填字段 + 压缩图 base64 */
    RecognizeResult recognize(byte[] originalImage);

    /** 仅压缩图片为 jpg（用于无识别时也能存图） */
    byte[] compress(byte[] originalImage);
}
