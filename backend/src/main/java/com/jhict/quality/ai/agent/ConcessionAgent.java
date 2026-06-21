package com.jhict.quality.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jhict.quality.ai.client.LlmClient;
import com.jhict.quality.ai.client.LlmRequest;
import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.ai.prompt.PromptTemplate;
import com.jhict.quality.enums.AiCallSource;
import com.jhict.quality.vo.ConcessionAssessmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class ConcessionAgent {

    private static final int MAX_ROUNDS = 3;

    @Resource
    private ConcessionAgentToolKit toolKit;

    @Resource
    private LlmClient llmClient;

    @Resource
    private PromptRegistry promptRegistry;

    public ConcessionAssessmentVO assess(String judgmentId, String concessionId) {
        Map<String, Object> toolResults = new LinkedHashMap<>();
        toolResults.put("judgmentContext", toolKit.getJudgmentContext(judgmentId));
        toolResults.put("standardLimits", toolKit.getStandardLimits(judgmentId));
        toolResults.put("historicalCases", toolKit.getHistoricalCases(judgmentId));
        toolResults.put("customerInfo", toolKit.getCustomerInfo(judgmentId));

        PromptTemplate template = promptRegistry.getActiveTemplate("concession-agent");
        String toolJson = JSON.toJSONString(toolResults);
        String judgmentJson = JSON.toJSONString(toolResults.get("judgmentContext"));

        LlmRequest request = LlmRequest.builder()
                .callSource(AiCallSource.CONCESSION_ASSESS)
                .promptKey("concession-agent")
                .systemPrompt(template != null ? template.getSystem() : null)
                .userPrompt(template != null
                        ? template.renderUser("judgmentContext", judgmentJson, "toolResults", toolJson)
                        : judgmentJson + "\n" + toolJson)
                .bizRefId(judgmentId)
                .build();

        LlmResponse response = llmClient.chat(request);
        ConcessionAssessmentVO vo = parseResponse(response, toolResults);
        vo.setJudgmentId(judgmentId);
        vo.setAuditLogId(response.getAuditLogId());
        vo.setDegraded(response.isDegraded() || !response.isSuccess());
        if (vo.getDegraded()) {
            applyDegradedChecklist(vo, toolResults);
        }
        return vo;
    }

    private ConcessionAssessmentVO parseResponse(LlmResponse response, Map<String, Object> toolResults) {
        ConcessionAssessmentVO vo = new ConcessionAssessmentVO();
        if (!response.isSuccess() || !StringUtils.hasText(response.getContent())) {
            applyDegradedChecklist(vo, toolResults);
            vo.setDegraded(true);
            return vo;
        }
        try {
            String content = response.getContent().trim();
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                content = content.substring(start, end + 1);
            }
            JSONObject json = JSON.parseObject(content);
            vo.setRiskLevel(json.getString("riskLevel"));
            vo.setCustomerImpact(json.getString("customerImpact"));
            vo.setSuggestedConditions(json.getString("suggestedConditions"));
            vo.setConfidenceLevel(json.getString("confidenceLevel"));
            vo.setHistoricalCases(JSON.toJSONString(toolResults.get("historicalCases")));
        } catch (Exception e) {
            log.warn("解析让步 Agent 输出失败，使用降级", e);
            applyDegradedChecklist(vo, toolResults);
            vo.setDegraded(true);
        }
        if (!StringUtils.hasText(vo.getRiskLevel())) {
            vo.setRiskLevel("MEDIUM");
        }
        return vo;
    }

    private void applyDegradedChecklist(ConcessionAssessmentVO vo, Map<String, Object> toolResults) {
        vo.setRiskLevel("MEDIUM");
        vo.setCustomerImpact("偏差在让步范围内，需评估客户用途与后续加工要求。");
        vo.setSuggestedConditions("1. 限制用于非关键结构部位；2. 加强入库复检；3. 客户书面确认让步接收。");
        vo.setConfidenceLevel("MEDIUM");
        vo.setHistoricalCases(JSON.toJSONString(toolResults.get("historicalCases")));
    }
}
