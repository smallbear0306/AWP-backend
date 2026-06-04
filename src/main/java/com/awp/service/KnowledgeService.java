package com.awp.service;

import com.awp.dto.KnowledgeCard;
import com.awp.util.EmbeddingClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 识别知识库：启动时加载规则卡片并向量化，按查询做余弦相似度检索（RAG）。
 * 规则以数据(cards.json)存放，新增图片类型只需加卡片，不改后端代码。
 */
@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    private final EmbeddingClient embeddingClient;
    private final ObjectMapper mapper = new ObjectMapper();

    private List<KnowledgeCard> cards = new ArrayList<>();
    private List<float[]> vectors = new ArrayList<>();
    private boolean vectorReady = false;

    public KnowledgeService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    @PostConstruct
    public void init() {
        try (var in = new ClassPathResource("knowledge/cards.json").getInputStream()) {
            cards = List.of(mapper.readValue(in, KnowledgeCard[].class));
            log.info("加载知识卡片 {} 张", cards.size());
        } catch (Exception e) {
            log.error("加载知识卡片失败", e);
            cards = new ArrayList<>();
        }
        if (!cards.isEmpty() && embeddingClient.isConfigured()) {
            try {
                vectors = embeddingClient.embed(cards.stream().map(KnowledgeCard::getScenario).toList());
                vectorReady = vectors.size() == cards.size();
                log.info("知识卡片向量化完成，可用={}", vectorReady);
            } catch (Exception e) {
                log.warn("知识卡片向量化失败，检索将回退为全量注入: {}", e.getMessage());
            }
        }
    }

    /**
     * 检索与查询最相关的 topK 张卡片；向量不可用时回退返回全部卡片。
     */
    public List<KnowledgeCard> retrieve(String query, int topK) {
        if (!vectorReady || query == null || query.isBlank()) {
            return cards;
        }
        try {
            float[] q = embeddingClient.embed(query);
            record Scored(KnowledgeCard card, double score) {
            }
            List<Scored> scored = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                scored.add(new Scored(cards.get(i), EmbeddingClient.cosine(q, vectors.get(i))));
            }
            scored.sort(Comparator.comparingDouble(Scored::score).reversed());
            List<KnowledgeCard> out = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, scored.size()); i++) {
                out.add(scored.get(i).card());
                log.debug("RAG 命中 {} score={}", scored.get(i).card().getKey(), scored.get(i).score());
            }
            return out;
        } catch (Exception e) {
            log.warn("检索失败，回退全量: {}", e.getMessage());
            return cards;
        }
    }
}
