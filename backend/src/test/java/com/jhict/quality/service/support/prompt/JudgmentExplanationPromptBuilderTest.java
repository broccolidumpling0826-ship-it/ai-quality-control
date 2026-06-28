package com.jhict.quality.service.support.prompt;

import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JudgmentExplanationPromptBuilderTest {

    private JudgmentExplanationPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new JudgmentExplanationPromptBuilder(PromptRegistryTestSupport.createLoadedRegistry());
    }

    @Test
    void build_shouldMatchLegacyPromptShape() {
        QcJudgmentResultVO vo = new QcJudgmentResultVO();
        vo.setRuleExplanation("抗拉强度超限");
        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorName("抗拉强度");
        evidence.setTestValue(new BigDecimal("430"));
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));
        evidence.setDeviation(new BigDecimal("0"));
        evidence.setTriggerRule("UPPER");
        vo.setEvidences(Collections.singletonList(evidence));

        QcJudgmentResult result = new QcJudgmentResult();
        result.setId("JR-001");
        result.setJudgmentType("UNQUALIFIED");

        AiSourceReferenceVO citation = new AiSourceReferenceVO();
        citation.setClauseId("c1");
        citation.setClauseNo("5.1");
        citation.setParagraphText("抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");

        ModelChatRequest request = builder.build(vo, result, Collections.singletonList(citation));

        assertEquals("judgment-explanation-v3", request.getPromptVersion());
        assertEquals("JUDGMENT_EXPLANATION", request.getBusinessType());
        String user = request.getMessages().get(0).getContent();
        assertTrue(user.startsWith("判定结论：UNQUALIFIED\n规则解释：抗拉强度超限\n\n指标依据：\n"));
        assertTrue(user.contains("抗拉强度 实测=430"));
        assertTrue(user.contains("[1]"));
        assertTrue(user.contains("来源编号如[1]"));
    }
}
