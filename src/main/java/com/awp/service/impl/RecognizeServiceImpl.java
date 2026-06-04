package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.RecognizeResult;
import com.awp.entity.Category;
import com.awp.mapper.CategoryMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 截图识别实现：后端压缩 → 拼分类树/规则 Prompt → 调 youtu-vita → 解析 → 映射分类。
 */
@Service
public class RecognizeServiceImpl implements RecognizeService {

    private static final Logger log = LoggerFactory.getLogger(RecognizeServiceImpl.class);

    private final CategoryMapper categoryMapper;
    private final VitaClient vitaClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${awp.vita.max-edge:1280}")
    private int maxEdge;
    @Value("${awp.vita.quality:0.8}")
    private float quality;

    public RecognizeServiceImpl(CategoryMapper categoryMapper, VitaClient vitaClient) {
        this.categoryMapper = categoryMapper;
        this.vitaClient = vitaClient;
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
            String content = vitaClient.recognize(jpg, buildPrompt(cats));
            parseInto(result, content, cats);
            result.setRecognized(true);
        } catch (Exception e) {
            // 识别失败也返回压缩图，让用户手动填
            log.warn("识别失败: {}", e.getMessage());
            result.setRecognized(false);
            result.setMessage("自动识别失败，请手动填写：" + e.getMessage());
        }
        return result;
    }

    /** 拼接提示词：分类清单 + 规则（含拼多多订单号取日期） */
    private String buildPrompt(List<Category> cats) {
        // 组织 一级: 二级,二级 文本
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
                sb.append(c.getName());
            }
        }
        StringBuilder expense = new StringBuilder();
        StringBuilder income = new StringBuilder();
        for (Category root : roots.values()) {
            StringBuilder target = root.getType() != null && root.getType() == 1 ? income : expense;
            target.append("  ").append(root.getName()).append(": ")
                    .append(children.get(root.getId())).append("\n");
        }

        return "你是记账助手。请识别这张支付/购物/银行截图中的交易信息。\n"
                + "可选分类体系（请从中选最贴切的【二级分类】，并给出其所属一级）：\n"
                + "支出:\n" + expense
                + "收入:\n" + income
                + "规则：\n"
                + "- type: \"支出\" 或 \"收入\"\n"
                + "- amount: 实际影响钱包的金额(元,正数)。一般=交易主金额\n"
                + "- gross / fee: 仅当截图为含税收入(个税/工资/劳务报酬，含\"收入\"且含\"已申报税额/纳税额\")时填：gross=申报收入、fee=已申报税额；其余场景两者都填 null。此时 amount 取 gross−fee\n"
                + "  注意：减除费用、专项扣除、免税收入、费用 等仅用于计税，不要从金额里扣除\n"
                + "- date: \"YYYY-MM-DD\"，取支付/交易发生的日期；个税/纳税明细类优先取【申报日期】(不要取税款所属期)；若无明确日期但有订单编号且形如\"20260602-xxxxxx\"，则取\"-\"前的8位作为年月日\n"
                + "- counterparty: 收款方/商家/扣缴义务人名称（不要填付款人本人）\n"
                + "- channel: 支付渠道(支付宝/微信/银行/花呗/零钱等)\n"
                + "- category_l1 / category_l2: 必须从上面给定清单中选；选不到填 null\n"
                + "- summary: 一句话说明(商品/用途)\n"
                + "严格只输出 JSON，不要任何多余文字、不要 markdown 围栏。";
    }

    /** 解析模型返回并映射分类 */
    private void parseInto(RecognizeResult r, String content, List<Category> cats) throws Exception {
        String txt = content == null ? "" : content.trim();
        int s = txt.indexOf('{');
        int e = txt.lastIndexOf('}');
        if (s < 0 || e <= s) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "返回非 JSON");
        }
        JsonNode j = mapper.readTree(txt.substring(s, e + 1));

        // type
        String typeStr = text(j, "type");
        Integer type = null;
        if (typeStr != null) {
            if (typeStr.contains("收")) type = 1;
            else if (typeStr.contains("支")) type = 0;
        }
        r.setType(type);

        // amount：含税收入场景由后端做减法(gross-fee)，确定性，不依赖模型算术
        BigDecimal amount = num(j, "amount");
        BigDecimal gross = num(j, "gross");
        BigDecimal fee = num(j, "fee");
        if (gross != null && fee != null) {
            amount = gross.subtract(fee);
        }
        if (amount != null) {
            r.setAmount(amount.abs());
        }
        r.setRecordDate(text(j, "date"));
        r.setCategoryL1(text(j, "category_l1"));
        r.setCategoryL2(text(j, "category_l2"));

        String counterparty = text(j, "counterparty");
        String summary = text(j, "summary");
        String remark = summary != null ? summary : counterparty;
        if (counterparty != null && summary != null && !summary.contains(counterparty)) {
            remark = counterparty + "·" + summary;
        }
        r.setRemark(remark);

        // 映射二级分类 -> id（按类型 + 名称匹配）
        Category leaf = matchLeaf(cats, r.getCategoryL2(), type);
        if (leaf != null) {
            r.setCategoryId(leaf.getId());
            r.setParentCategoryId(leaf.getParentId());
        }
    }

    /** 在可见分类里按名称(同类型)匹配一个二级分类 */
    private Category matchLeaf(List<Category> cats, String l2, Integer type) {
        if (l2 == null || l2.isBlank()) return null;
        String key = l2.trim();
        Category exact = null;
        Category contains = null;
        for (Category c : cats) {
            if (c.getParentId() == null) continue;            // 只看二级
            if (type != null && !type.equals(c.getType())) continue;
            String name = c.getName() == null ? "" : c.getName().trim();
            if (name.equals(key)) {
                exact = c;
                break;
            }
            if (contains == null && (name.contains(key) || key.contains(name))) {
                contains = c;
            }
        }
        return exact != null ? exact : contains;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s;
    }

    /** 解析数字字段为 BigDecimal，无效返回 null */
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
