package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.KnowledgeCard;
import com.awp.dto.RecognizeResult;
import com.awp.entity.Category;
import com.awp.mapper.CategoryMapper;
import com.awp.service.KnowledgeService;
import com.awp.service.RecognizeService;
import com.awp.util.ImageUtil;
import com.awp.util.VitaClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 截图识别（RAG + 多轮）。
 * 后端只做：压缩、编排 3 轮模型调用、结构校验、把模型产出的数据交给上层入库。
 * 所有业务逻辑/算术（如个税净收入）由模型依据检索到的知识卡片自行完成，后端不写死任何按类型的规则。
 */
@Service
public class RecognizeServiceImpl implements RecognizeService {

    private static final Logger log = LoggerFactory.getLogger(RecognizeServiceImpl.class);

    private final CategoryMapper categoryMapper;
    private final VitaClient vitaClient;
    private final KnowledgeService knowledgeService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${awp.vita.max-edge:1280}")
    private int maxEdge;
    @Value("${awp.vita.quality:0.8}")
    private float quality;
    @Value("${awp.embedding.top-k:2}")
    private int topK;

    public RecognizeServiceImpl(CategoryMapper categoryMapper, VitaClient vitaClient,
                                KnowledgeService knowledgeService) {
        this.categoryMapper = categoryMapper;
        this.vitaClient = vitaClient;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public byte[] compress(byte[] originalImage) {
        try {
            return ImageUtil.compressToJpeg(originalImage, maxEdge, quality);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "图片处理失败：" + e.getMessage());
        }
    }

    @Override
    public RecognizeResult recognize(byte[] originalImage) {
        Long userId = UserContext.getUserId();
        byte[] jpg = compress(originalImage);

        RecognizeResult result = new RecognizeResult();
        result.setImageBase64(Base64.getEncoder().encodeToString(jpg));

        List<Category> cats = categoryMapper.listVisible(userId, null);
        try {
            // 第1轮：判断场景
            String scenario = vitaClient.recognize(jpg,
                    "你在看一张用于个人记账的截图。用一句中文描述它属于什么平台/场景，并列出包含的关键信息"
                            + "（如金额、税额、订单号、收支方向、日期等）。只输出这一句话。");

            // RAG：按场景检索知识卡片
            List<KnowledgeCard> hits = knowledgeService.retrieve(scenario, topK);

            // 第2轮：依据规则 + 分类清单，产出最终 JSON
            String json2 = vitaClient.recognize(jpg, buildExtractPrompt(hits, cats));

            // 第3轮：自检纠错（强调逐位核对，减少 OCR/算术误差）
            String json3 = vitaClient.recognize(jpg,
                    "这是你刚才对本截图给出的记账结果 JSON：\n" + extractJson(json2) + "\n"
                            + "请对照原图严格自检并改正：\n"
                            + "1) 金额：在图中逐位核对每一个数字与小数点；若涉及计算(如含税收入=申报收入−已申报税额)，重新把两个原始数字读准后再算一遍。\n"
                            + "2) 日期：逐位核对年/月/日，特别是年份(如 2026 不要看成 2024)；若来自订单编号，核对'-'前 8 位。\n"
                            + "3) 收支方向、分类 id 是否合理。\n"
                            + "返回修正后的完整 JSON（无误则原样）。严格只输出 JSON，不要多余文字、不要 markdown 围栏。");

            parseInto(result, json3, cats);
            result.setRecognized(true);
        } catch (Exception e) {
            log.warn("识别失败: {}", e.getMessage());
            result.setRecognized(false);
            result.setMessage("自动识别失败，请手动填写：" + e.getMessage());
        }
        return result;
    }

    /** 第2轮提示词：注入检索到的规则卡片 + 带 id 的分类清单 + 输出字段 */
    private String buildExtractPrompt(List<KnowledgeCard> cards, List<Category> cats) {
        StringBuilder rules = new StringBuilder();
        for (KnowledgeCard c : cards) {
            rules.append("- ").append(c.getTitle()).append("：").append(c.getRule()).append("\n");
        }
        return "你是记账助手，请从这张截图提取记账所需信息，并自行完成其中需要的计算。\n"
                + "【处理规则】(严格遵守)：\n" + rules
                + "【可选分类】(从中挑最贴切的二级分类，返回它的数字 id)：\n" + buildCategoryList(cats)
                + "【输出字段】严格只输出 JSON：\n"
                + "type: \"支出\" 或 \"收入\"\n"
                + "amount: 最终入账金额(数字,正数，已按规则算好，不要附带单位)\n"
                + "date: \"YYYY-MM-DD\"\n"
                + "categoryId: 选中的二级分类的数字 id；选不到填 null\n"
                + "counterparty: 收款方/商家/扣缴义务人（不要填付款人本人）\n"
                + "channel: 支付渠道(支付宝/微信/银行/花呗/零钱等)，无则 null\n"
                + "summary: 一句话说明\n"
                + "不要输出多余文字、不要 markdown 围栏。";
    }

    /** 分类清单文本：一级: 二级#id, ... */
    private String buildCategoryList(List<Category> cats) {
        Map<Long, Category> roots = new LinkedHashMap<>();
        Map<Long, StringBuilder> children = new LinkedHashMap<>();
        for (Category c : cats) {
            if (c.getParentId() == null) {
                roots.put(c.getId(), c);
                children.putIfAbsent(c.getId(), new StringBuilder());
            }
        }
        for (Category c : cats) {
            if (c.getParentId() != null && children.containsKey(c.getParentId())) {
                StringBuilder sb = children.get(c.getParentId());
                if (sb.length() > 0) sb.append("，");
                sb.append(c.getName()).append("#").append(c.getId());
            }
        }
        StringBuilder expense = new StringBuilder();
        StringBuilder income = new StringBuilder();
        for (Category root : roots.values()) {
            StringBuilder t = (root.getType() != null && root.getType() == 1) ? income : expense;
            t.append("  ").append(root.getName()).append(": ").append(children.get(root.getId())).append("\n");
        }
        return "支出:\n" + expense + "收入:\n" + income;
    }

    /** 解析最终 JSON 写入结果；分类 id 仅做归属/合法性校验（非业务逻辑） */
    private void parseInto(RecognizeResult r, String content, List<Category> cats) throws Exception {
        JsonNode j = mapper.readTree(extractJson(content));

        String typeStr = text(j, "type");
        Integer type = null;
        if (typeStr != null) {
            if (typeStr.contains("收")) type = 1;
            else if (typeStr.contains("支")) type = 0;
        }
        r.setType(type);

        BigDecimal amount = num(j, "amount");
        if (amount != null) r.setAmount(amount.abs());

        r.setRecordDate(text(j, "date"));

        String counterparty = text(j, "counterparty");
        String summary = text(j, "summary");
        String remark = summary != null ? summary : counterparty;
        if (counterparty != null && summary != null && !summary.contains(counterparty)) {
            remark = counterparty + "·" + summary;
        }
        r.setRemark(remark);

        // 校验模型返回的 categoryId：必须是当前用户可见、二级(叶子)、类型一致
        Map<Long, Category> byId = new HashMap<>();
        for (Category c : cats) byId.put(c.getId(), c);
        BigDecimal cidNum = num(j, "categoryId");
        if (cidNum != null) {
            Category leaf = byId.get(cidNum.longValue());
            if (leaf != null && leaf.getParentId() != null
                    && (type == null || type.equals(leaf.getType()))) {
                r.setCategoryId(leaf.getId());
                r.setParentCategoryId(leaf.getParentId());
                r.setCategoryL2(leaf.getName());
                Category parent = byId.get(leaf.getParentId());
                if (parent != null) r.setCategoryL1(parent.getName());
            }
        }
    }

    /** 从可能带 markdown 围栏的文本里截取 JSON 主体 */
    private String extractJson(String content) {
        String txt = content == null ? "" : content.trim();
        int s = txt.indexOf('{');
        int e = txt.lastIndexOf('}');
        if (s < 0 || e <= s) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "模型返回非 JSON");
        }
        return txt.substring(s, e + 1);
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s;
    }

    private BigDecimal num(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null) return null;
        try {
            return new BigDecimal(s.replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
