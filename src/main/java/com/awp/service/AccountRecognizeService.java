package com.awp.service;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.dto.AccountRecognizeResult;
import com.awp.util.ImageUtil;
import com.awp.util.VitaClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户截图识别：识别账户类型/银行/储蓄or信用/当前余额，用于新建账户或划账预填。
 * 同样只让模型分析，后端不写按账户类型的规则。
 */
@Service
public class AccountRecognizeService {

    private static final Logger log = LoggerFactory.getLogger(AccountRecognizeService.class);

    private final VitaClient vitaClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${awp.vita.max-edge:1280}")
    private int maxEdge;
    @Value("${awp.vita.quality:0.8}")
    private float quality;

    public AccountRecognizeService(VitaClient vitaClient) {
        this.vitaClient = vitaClient;
    }

    public AccountRecognizeResult recognize(byte[] originalImage) {
        AccountRecognizeResult r = new AccountRecognizeResult();
        try {
            byte[] jpg = ImageUtil.compressToJpeg(originalImage, maxEdge, quality);
            String content = vitaClient.recognize(jpg, prompt());
            parse(r, content);
            r.setRecognized(true);
        } catch (Exception e) {
            log.warn("账户识别失败: {}", e.getMessage());
            r.setRecognized(false);
            r.setMessage("自动识别失败，请手动填写：" + e.getMessage());
        }
        return r;
    }

    private String prompt() {
        return "识别这张账户/钱包/银行卡/余额截图。一张图可能含【多个账户】(如银行总览页的多张卡、可用余额与理财余额、不同子账户)，请分别列出，不要合并相加。严格只输出 JSON 对象 {\"accounts\":[ ... ]}，每个元素字段：\n"
                + "type: 从[储蓄卡,信用卡,支付宝余额,微信余额,花呗,余额宝,零钱通,理财账户,饭卡,现金,其他]中选最匹配的一个\n"
                + "bank: 若为银行卡/银行理财，填银行名(如 招商银行、浦发银行)，否则 null\n"
                + "kind: 0 储蓄类(借记卡/各类余额/余额宝/零钱通/理财)，1 信用类(信用卡/花呗)\n"
                + "balance: 该账户当前余额(数字,正数，逐位读准)；花呗/信用卡只显示待还款则填 0\n"
                + "name: 简短账户名(如 浦发银行储蓄卡、浦发灵活+理财、支付宝余额)\n"
                + "不要输出多余文字、不要 markdown 围栏。";
    }

    private void parse(AccountRecognizeResult r, String content) throws Exception {
        String txt = content == null ? "" : content.trim();
        int s = txt.indexOf('{');
        int e = txt.lastIndexOf('}');
        if (s < 0 || e <= s) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "模型返回非 JSON");
        }
        JsonNode root = mapper.readTree(txt.substring(s, e + 1));
        JsonNode arr = root.path("accounts");
        if (!arr.isArray() || arr.isEmpty()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "模型未返回账户");
        }
        List<AccountRecognizeResult.Item> items = new ArrayList<>();
        for (JsonNode j : arr) {
            AccountRecognizeResult.Item it = new AccountRecognizeResult.Item();
            it.setName(text(j, "name"));
            it.setType(text(j, "type"));
            it.setBank(text(j, "bank"));
            String kind = text(j, "kind");
            it.setKind(kind != null && kind.contains("1") ? 1 : 0);
            String bal = text(j, "balance");
            if (bal != null) {
                try {
                    it.setBalance(new BigDecimal(bal.replaceAll("[^0-9.\\-]", "")).abs());
                } catch (Exception ignore) {
                }
            }
            items.add(it);
        }
        r.setItems(items);
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String str = v.asText();
        return (str == null || str.isBlank() || "null".equalsIgnoreCase(str)) ? null : str;
    }
}
