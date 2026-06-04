package com.awp.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文本向量化客户端（阿里云百炼 DashScope，OpenAI 兼容 /embeddings）。
 */
@Component
public class EmbeddingClient {

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();

    public EmbeddingClient(@Value("${awp.embedding.base-url}") String baseUrl,
                           @Value("${awp.embedding.model}") String model,
                           @Value("${awp.embedding.api-key:}") String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** 批量向量化，返回与输入同序的向量列表 */
    public List<float[]> embed(List<String> texts) {
        try {
            String json = mapper.writeValueAsString(Map.of("model", model, "input", texts));
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("embedding 状态码 " + resp.statusCode() + ": " + resp.body());
            }
            JsonNode data = mapper.readTree(resp.body()).path("data");
            List<float[]> out = new ArrayList<>();
            for (JsonNode item : data) {
                JsonNode arr = item.path("embedding");
                float[] v = new float[arr.size()];
                for (int i = 0; i < arr.size(); i++) v[i] = (float) arr.get(i).asDouble();
                out.add(v);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("embedding 调用失败: " + e.getMessage(), e);
        }
    }

    public float[] embed(String text) {
        return embed(List.of(text)).get(0);
    }

    /** 余弦相似度 */
    public static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
