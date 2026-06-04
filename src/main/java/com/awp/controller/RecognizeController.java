package com.awp.controller;

import com.awp.common.BusinessException;
import com.awp.common.Result;
import com.awp.common.ResultCode;
import com.awp.dto.RecognizeResult;
import com.awp.service.RecognizeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 截图识别接口（需登录）。上传截图 → 后端压缩 → AI 识别 → 返回预填字段 + 压缩图。
 */
@RestController
@RequestMapping("/api/record")
public class RecognizeController {

    private final RecognizeService recognizeService;

    public RecognizeController(RecognizeService recognizeService) {
        this.recognizeService = recognizeService;
    }

    @PostMapping("/recognize")
    public Result<RecognizeResult> recognize(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请上传图片");
        }
        try {
            return Result.success(recognizeService.recognize(file.getBytes()));
        } catch (IOException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "读取图片失败");
        }
    }
}
