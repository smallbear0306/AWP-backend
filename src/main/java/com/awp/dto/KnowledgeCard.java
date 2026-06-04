package com.awp.dto;

import lombok.Data;

/**
 * 识别知识卡片（规则以数据形式存放，新增图片类型只需加卡片，不改后端逻辑）。
 */
@Data
public class KnowledgeCard {
    private String key;
    private String title;
    private String scenario;  // 用于向量检索的场景关键词
    private String rule;      // 注入给模型的处理规则
}
