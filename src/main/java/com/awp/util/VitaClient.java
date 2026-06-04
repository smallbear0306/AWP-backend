package com.awp.util;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * youtu-vita 图像识别客户端（OpenAI 兼容 /chat/completions）。
 */
@Component
public class VitaClient {

    private static final Logger log = LoggerFactory.getLogger(VitaClient.class);

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final boolean disableThinking;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public VitaClient(@Value("${awp.vita.base-url}") String baseUrl,
                      @Value("${awp.vita.model}") String model,
                      @Value("${awp.vita.api-key:}") String apiKey,
                      @Value("${awp.vita.disable-thinking:false}") boolean disableThinking) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
        this.disableThinking = disableThinking;
    }

    /**
     * 发送一张 jpg 图片 + 文本提示，返回模型回复的纯文本内容。
     */
    public String recognize(byte[] jpegBytes, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "未配置图像识别 API Key");
        }
        String dataUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(jpegBytes);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("stream", false);
        // 关闭深度思考(豆包等支持)，大幅提速；非支持的模型可在配置里关掉此开关
        if (disableThinking) {
            body.put("thinking", Map.of("type", "disabled"));
        }
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", List.of(
                        Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)),
                        Map.of("type", "text", "text", prompt)
                )
        )));
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("vita 调用失败 status={} body={}", resp.statusCode(), resp.body());
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "图像识别服务返回错误");
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "图像识别返回为空");
            }
            return content.asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("vita 调用异常", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "图像识别调用异常: " + e.getMessage());
        }
    }
}
