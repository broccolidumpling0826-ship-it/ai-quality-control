package com.jhict.quality.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.jhict.quality.ai.client.LlmClient;
import com.jhict.quality.ai.client.LlmRequest;
import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.ai.prompt.PromptTemplate;
import com.jhict.quality.entity.QcJudgmentEvidence;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.enums.AiCallSource;
import com.jhict.quality.mapper.QcJudgmentEvidenceMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.vo.AiSuggestionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReinspectionAdvisor {

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private LlmClient llmClient;

    @Resource
    private PromptRegistry promptRegistry;

    public AiSuggestionVO advise(String judgmentId, String suggestionType) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null) {
            throw new IllegalArgumentException("判定不存在");
        }
        List<QcJudgmentEvidence> evidences = judgmentEvidenceMapper.findByJudgmentId(judgmentId);
        List<String> failedIndicators = evidences.stream()
                .filter(e -> e.getIsPassed() != null && e.getIsPassed() == 0)
                .map(QcJudgmentEvidence::getIndicatorId)
                .collect(Collectors.toList());

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("judgmentType", judgment.getJudgmentType());
        context.put("failedIndicators", failedIndicators);
        context.put("suggestionType", suggestionType);

        PromptTemplate template = promptRegistry.getActiveTemplate("judgment-explain");
        String contextJson = JSON.toJSONString(context);
        LlmRequest request = LlmRequest.builder()
                .callSource("REINSPECTION".equals(suggestionType)
                        ? AiCallSource.REINSPECTION_ADVICE : AiCallSource.REJUDGMENT_ADVICE)
                .promptKey("judgment-explain")
                .systemPrompt("你是复检/改判建议助手。根据判定证据给出建议动作(YES/NO/CONDITIONAL)与关注指标，不得替代审批。")
                .userPrompt("判定上下文：\n" + contextJson)
                .bizRefId(judgmentId)
                .build();

        LlmResponse response = llmClient.chat(request);
        AiSuggestionVO vo = new AiSuggestionVO();
        vo.setSuggestionType(suggestionType);
        vo.setRefId(judgmentId);
        vo.setAuditLogId(response.getAuditLogId());
        vo.setDegraded(response.isDegraded() || !response.isSuccess());

        if (response.isSuccess() && StringUtils.hasText(response.getContent()) && !response.isDegraded()) {
            vo.setRecommendedAction(inferAction(judgment.getJudgmentType()));
            vo.setReasonText(response.getContent());
            vo.setConfidenceLevel("MEDIUM");
        } else {
            vo.setRecommendedAction(inferAction(judgment.getJudgmentType()));
            vo.setReasonText(buildDegradedReason(judgment, failedIndicators));
            vo.setConfidenceLevel("LOW");
            vo.setDegraded(true);
        }
        vo.setFocusIndicators(failedIndicators.isEmpty()
                ? Collections.singletonList("ind001") : failedIndicators);
        return vo;
    }

    private String inferAction(String judgmentType) {
        if ("NEED_REINSPECTION".equals(judgmentType) || "UNQUALIFIED".equals(judgmentType)) {
            return "YES";
        }
        if ("CAN_CONCESSION".equals(judgmentType)) {
            return "CONDITIONAL";
        }
        return "NO";
    }

    private String buildDegradedReason(QcJudgmentResult judgment, List<String> failedIndicators) {
        StringBuilder sb = new StringBuilder("规则建议：");
        sb.append("当前判定为 ").append(judgment.getJudgmentType()).append("。");
        if (!failedIndicators.isEmpty()) {
            sb.append(" 建议重点关注指标：").append(String.join(", ", failedIndicators)).append("。");
        }
        if ("NEED_REINSPECTION".equals(judgment.getJudgmentType())) {
            sb.append(" 建议发起复检确认实测值。");
        }
        return sb.toString();
    }
}
