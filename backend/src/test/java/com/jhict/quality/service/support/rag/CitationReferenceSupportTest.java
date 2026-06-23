package com.jhict.quality.service.support.rag;

import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitationReferenceSupportTest {

    @Test
    void rankAndLimit_shouldPreferIndicatorClauseOverScopeClause() {
        AiSourceReferenceVO scope = ref("scope", "1 范围 本文件规定了 Q235B 冷轧薄板及钢带的化学成分、力学性能。");
        AiSourceReferenceVO limit = ref("limit", "5.1 抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");
        limit.setIndicatorCode("Rm");
        limit.setIndicatorName("抗拉强度");

        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorCode("Rm");
        evidence.setIndicatorName("抗拉强度");
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));

        List<AiSourceReferenceVO> ranked = CitationReferenceSupport.rankAndLimit(
                Arrays.asList(scope, limit), Arrays.asList(evidence), 2);

        assertEquals("limit", ranked.get(0).getClauseId());
        assertEquals(1, ranked.size());
    }

    @Test
    void isGroundedCitedAnswer_shouldRejectScopeCitationForNumericLimits() {
        AiSourceReferenceVO scope = ref("scope", "1 范围 本文件规定了 Q235B 冷轧薄板及钢带的化学成分、力学性能。");
        String answer = "抗拉强度实测值 430 MPa，在标准下限 370 MPa 和上限 510 MPa 范围内[1]。";

        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorName("抗拉强度");
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));

        assertFalse(CitationReferenceSupport.isGroundedCitedAnswer(
                answer, Arrays.asList(scope), Arrays.asList(evidence)));
    }

    @Test
    void isGroundedCitedAnswer_shouldAcceptLimitClauseCitation() {
        AiSourceReferenceVO limit = ref("limit", "5.1 抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");
        String answer = "抗拉强度实测值 430 MPa，在标准下限 370 MPa 和上限 510 MPa 范围内[1]。";

        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorName("抗拉强度");
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));

        assertTrue(CitationReferenceSupport.isGroundedCitedAnswer(
                answer, Arrays.asList(limit), Arrays.asList(evidence)));
    }

    @Test
    void isGroundedCitedAnswer_shouldRejectScopeCitationForRagAnswer() {
        AiSourceReferenceVO scope = ref("scope", "1 范围 本文件规定了 Q235B 冷轧薄板及钢带的化学成分、力学性能。");
        String answer = "抗拉强度合格范围为 370 MPa 至 510 MPa[1]。";
        assertFalse(CitationReferenceSupport.isGroundedCitedAnswer(
                answer, Collections.singletonList(scope), null));
    }

    @Test
    void appendCitationAnswerRules_shouldIncludeScopeRestriction() {
        StringBuilder prompt = new StringBuilder();
        CitationReferenceSupport.appendCitationAnswerRules(
                prompt, CitationReferenceSupport.CitationPromptStyle.STANDARD_RAG);
        assertTrue(prompt.toString().contains("范围"));
        assertTrue(prompt.toString().contains("[1]"));
    }

    @Test
    void isGroundedCitedAnswer_shouldRejectDefinitionClauseCitationForNumericLimits() {
        AiSourceReferenceVO definition = ref("def",
                "3.1 抗拉强度 Rm：试样在拉伸过程中对应的最大力时的应力，单位为 MPa。");
        AiSourceReferenceVO limit = ref("limit",
                "6.1 Q235B 冷轧板及钢带力学性能。抗拉强度 Rm 低于 370MPa 或高于 510MPa 时判定不合格。");
        String answer = "抗拉强度实测430 MPa，处于370至510 MPa的范围内，符合要求[3]。";

        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorName("抗拉强度");
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));
        evidence.setTestValue(new BigDecimal("430"));

        assertFalse(CitationReferenceSupport.isGroundedCitedAnswer(
                answer, Arrays.asList(limit, limit, definition), Arrays.asList(evidence)));
    }

    @Test
    void rankAndLimit_shouldDropDefinitionClauseWhenLimitClauseExists() {
        AiSourceReferenceVO definition = ref("def",
                "3.1 抗拉强度 Rm：试样在拉伸过程中对应的最大力时的应力，单位为 MPa。");
        AiSourceReferenceVO limit = ref("limit", "6.1 抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");
        limit.setIndicatorName("抗拉强度");

        QcJudgmentResultVO.EvidenceVO evidence = new QcJudgmentResultVO.EvidenceVO();
        evidence.setIndicatorName("抗拉强度");
        evidence.setLowerLimit(new BigDecimal("370"));
        evidence.setUpperLimit(new BigDecimal("510"));

        List<AiSourceReferenceVO> ranked = CitationReferenceSupport.rankAndLimit(
                Arrays.asList(definition, limit), Arrays.asList(evidence), 2);

        assertEquals(1, ranked.size());
        assertEquals("limit", ranked.get(0).getClauseId());
    }

    @Test
    void acceptTrustedCitedOutput_shouldRejectWhenCitationMarkerMissing() {
        AiSourceReferenceVO limit = ref("limit", "5.1 抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");
        assertNull(CitationReferenceSupport.acceptTrustedCitedOutput(
                "抗拉强度合格。", Collections.singletonList(limit), null));
    }

    @Test
    void acceptTrustedCitedOutput_shouldRejectWhenReferencesLackParagraph() {
        AiSourceReferenceVO incomplete = ref("incomplete", null);
        String answer = "抗拉强度合格范围为 370 MPa 至 510 MPa[1]。";
        assertNull(CitationReferenceSupport.acceptTrustedCitedOutput(
                answer, Collections.singletonList(incomplete), null));
    }

    @Test
    void filterActionableReferences_shouldDropDefinitionWhenLimitExists() {
        AiSourceReferenceVO definition = ref("def",
                "3.1 抗拉强度 Rm：试样在拉伸过程中对应的最大力时的应力，单位为 MPa。");
        AiSourceReferenceVO limit = ref("limit", "6.1 抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");
        List<AiSourceReferenceVO> filtered = CitationReferenceSupport.filterActionableReferences(
                Arrays.asList(definition, limit));
        assertEquals(1, filtered.size());
        assertEquals("limit", filtered.get(0).getClauseId());
    }

    @Test
    void buildJudgmentRuleExplanation_shouldIncludeConcessionIndicatorsWithCitation() {
        QcJudgmentResultVO.EvidenceVO rm = new QcJudgmentResultVO.EvidenceVO();
        rm.setIndicatorName("抗拉强度");
        rm.setTestValue(new BigDecimal("372"));
        rm.setLowerLimit(new BigDecimal("375"));
        rm.setUpperLimit(new BigDecimal("505"));
        rm.setIsPassed(0);
        rm.setTriggerRule("实测值 372.0 MPa 低于协议下限 375.0 MPa，但在让步范围内，触发 CAN_CONCESSION");

        AiSourceReferenceVO concession = ref("conc",
                "4.2 让步接收：抗拉强度 Rm、延伸率 A 等指标轻微偏差时可进入让步评审。");

        String explanation = CitationReferenceSupport.buildJudgmentRuleExplanation(
                "CAN_CONCESSION",
                Collections.singletonList(rm),
                Collections.singletonList(concession),
                3);

        assertTrue(explanation.contains("抗拉强度"));
        assertTrue(explanation.contains("372"));
        assertTrue(explanation.contains("[1]"));
        assertTrue(CitationReferenceSupport.acceptTrustedCitedOutput(
                explanation, Collections.singletonList(concession), Collections.singletonList(rm)) != null);
    }

    private static AiSourceReferenceVO ref(String id, String paragraph) {
        AiSourceReferenceVO reference = new AiSourceReferenceVO();
        reference.setClauseId(id);
        reference.setParagraphText(paragraph);
        return reference;
    }
}
