package com.jhict.quality.service.support.rag;

import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardRagSourceVO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 所有带 [n] 来源引用的 AI 场景统一工具：排序、Prompt 片段、引用准入校验。
 * <p>
 * 业务接入约定：
 * <ol>
 *   <li>调用模型前：{@link #rankAndLimit} 或 {@link #filterActionableReferences} 处理来源列表</li>
 *   <li>Prompt 中：{@link #appendNumberedCitationBlock} + {@link #appendCitationAnswerRules}</li>
 *   <li>模型/缓存/历史输出展示前：{@link #acceptTrustedCitedOutput} 统一校验</li>
 * </ol>
 */
public final class CitationReferenceSupport {

    public enum CitationPromptStyle {
        JUDGMENT_EXPLANATION,
        CERT_QA,
        STANDARD_RAG
    }

    private static final Pattern CITATION_MARKER = Pattern.compile("\\[(\\d+)\\]");
    private static final Pattern NUMERIC_LIMIT_IN_TEXT = Pattern.compile("\\d+(?:\\.\\d+)?\\s*(?:MPa|%)");
    private static final Pattern SENTENCE_NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    private CitationReferenceSupport() {
    }

    public static List<AiSourceReferenceVO> rankAndLimit(List<AiSourceReferenceVO> references,
                                                         List<QcJudgmentResultVO.EvidenceVO> evidences,
                                                         int limit) {
        if (references == null || references.isEmpty()) {
            return new ArrayList<>();
        }
        List<QcJudgmentResultVO.EvidenceVO> safeEvidences = evidences == null
                ? new ArrayList<QcJudgmentResultVO.EvidenceVO>() : evidences;
        int safeLimit = limit <= 0 ? references.size() : limit;
        List<ScoredReference> scored = new ArrayList<>();
        for (AiSourceReferenceVO reference : references) {
            scored.add(new ScoredReference(reference, scoreReference(reference, safeEvidences)));
        }
        scored.sort(Comparator.comparingInt(ScoredReference::getScore).reversed());
        boolean hasIndicatorClause = scored.stream().anyMatch(item -> item.getScore() >= 15);
        List<AiSourceReferenceVO> ranked = new ArrayList<>();
        for (ScoredReference item : scored) {
            if (hasIndicatorClause && item.getScore() < 0) {
                continue;
            }
            if (hasIndicatorClause && isDefinitionOnlyClause(nullToEmpty(item.getReference().getParagraphText()))) {
                continue;
            }
            if (hasIndicatorClause && isScopeOnlyClause(nullToEmpty(item.getReference().getParagraphText()))) {
                continue;
            }
            ranked.add(item.getReference());
            if (ranked.size() >= safeLimit) {
                break;
            }
        }
        if (ranked.isEmpty()) {
            return references.stream().limit(safeLimit).collect(Collectors.toList());
        }
        return ranked;
    }

    public static int scoreReference(AiSourceReferenceVO reference, List<QcJudgmentResultVO.EvidenceVO> evidences) {
        String text = reference == null ? "" : nullToEmpty(reference.getParagraphText());
        if (!StringUtils.hasText(text)) {
            return -100;
        }
        int score = 0;
        if (isScopeOnlyClause(text)) {
            score -= 25;
        }
        if (isDefinitionOnlyClause(text)) {
            score -= 30;
        }
        for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
            if (evidence == null) {
                continue;
            }
            if (StringUtils.hasText(evidence.getIndicatorCode())
                    && evidence.getIndicatorCode().equalsIgnoreCase(nullToEmpty(reference.getIndicatorCode()))) {
                score += 25;
            }
            if (StringUtils.hasText(evidence.getIndicatorName()) && text.contains(evidence.getIndicatorName())) {
                score += 20;
            }
            if (paragraphContainsLimit(text, evidence.getLowerLimit())) {
                score += 12;
            }
            if (paragraphContainsLimit(text, evidence.getUpperLimit())) {
                score += 12;
            }
            if (paragraphContainsLimit(text, evidence.getTestValue())) {
                score += 4;
            }
        }
        if (NUMERIC_LIMIT_IN_TEXT.matcher(text).find()) {
            score += 10;
        }
        if (text.contains("力学性能") && SENTENCE_NUMBER.matcher(text).find()) {
            score += 6;
        }
        return score;
    }

    public static boolean isDefinitionOnlyClause(String paragraphText) {
        if (!StringUtils.hasText(paragraphText)) {
            return true;
        }
        String text = paragraphText.trim();
        if (hasRequirementLimits(text) || isConcessionPolicyClause(text)) {
            return false;
        }
        return text.contains("术语")
                || text.contains("定义")
                || text.contains("是指")
                || text.contains("试样在")
                || text.contains("拉伸过程")
                || text.contains("对应的最大力")
                || text.contains("单位为")
                || text.contains("缩略语")
                || (text.contains("Rm") && text.contains("：") && !text.contains("至") && !text.contains("～"));
    }

    public static boolean isScopeOnlyClause(String paragraphText) {
        if (!StringUtils.hasText(paragraphText)) {
            return true;
        }
        String text = paragraphText.trim();
        boolean scopeLike = text.contains("范围") || text.contains("适用范围") || text.contains("本文件规定");
        return scopeLike && !hasRequirementLimits(text);
    }

    private static boolean hasRequirementLimits(String text) {
        return NUMERIC_LIMIT_IN_TEXT.matcher(text).find()
                || (text.contains("力学性能") && SENTENCE_NUMBER.matcher(text).find() && text.contains("MPa"))
                || text.contains("合格范围")
                || text.contains("应不低于")
                || text.contains("应为")
                || text.contains("实测值");
    }

    public static boolean paragraphHasRequirementLimits(String paragraphText) {
        return hasRequirementLimits(nullToEmpty(paragraphText));
    }

    public static boolean isNonActionableCitationClause(String paragraphText) {
        return isScopeOnlyClause(paragraphText) || isDefinitionOnlyClause(paragraphText);
    }

    /**
     * 带 [n] 标记的 AI 输出统一校验入口。通过则返回原文，否则返回 null（由上层降级或重新生成）。
     */
    public static String acceptTrustedCitedOutput(String content,
                                                List<AiSourceReferenceVO> citations,
                                                List<QcJudgmentResultVO.EvidenceVO> evidences) {
        if (!StringUtils.hasText(content) || citations == null || citations.isEmpty()) {
            return null;
        }
        if (citations.stream().anyMatch(c -> !StringUtils.hasText(c.getParagraphText()))) {
            return null;
        }
        if (!hasCitationMarker(content, citations)) {
            return null;
        }
        return isGroundedCitedAnswer(content, citations, evidences) ? content : null;
    }

    public static List<AiSourceReferenceVO> filterActionableReferences(List<AiSourceReferenceVO> references) {
        if (references == null || references.isEmpty()) {
            return new ArrayList<>();
        }
        boolean hasActionable = references.stream()
                .anyMatch(r -> paragraphHasRequirementLimits(r.getParagraphText()));
        if (!hasActionable) {
            return references;
        }
        List<AiSourceReferenceVO> filtered = references.stream()
                .filter(r -> !isNonActionableCitationClause(r.getParagraphText()))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? references : filtered;
    }

    public static List<StandardRagSourceVO> filterActionableRagSources(List<StandardRagSourceVO> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        boolean hasActionable = sources.stream()
                .anyMatch(s -> paragraphHasRequirementLimits(s.getParagraphText()));
        if (!hasActionable) {
            return sources;
        }
        List<StandardRagSourceVO> filtered = sources.stream()
                .filter(s -> !isNonActionableCitationClause(s.getParagraphText()))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? sources : filtered;
    }

    /**
     * 规则模板解释：汇总结论，并为异常/让步指标附上实测值、限值及可核验的 [n] 引用。
     */
    public static String buildJudgmentRuleExplanation(String judgmentType,
                                                      List<QcJudgmentResultVO.EvidenceVO> evidences,
                                                      List<AiSourceReferenceVO> citations,
                                                      int conflictCount) {
        if (JudgmentType.STANDARD_CONFLICT.getCode().equals(judgmentType)) {
            return "当前记录触发同优先级标准冲突，系统已阻断合格/不合格/让步类结论，需人工裁决控制标准后重新判定。";
        }
        if (evidences == null || evidences.isEmpty()) {
            return "未找到判定依据快照，无法生成完整规则解释，请人工复核检验记录和标准配置。";
        }
        long failedCount = evidences.stream()
                .filter(e -> !Integer.valueOf(1).equals(e.getIsPassed()))
                .count();
        long concessionCount = evidences.stream()
                .filter(e -> JudgmentExplainConstants.isConcessionTriggerRule(e.getTriggerRule()))
                .count();
        StringBuilder explanation = new StringBuilder();
        explanation.append("系统按结构化标准完成规则判定，最终结论为 ")
                .append(judgmentType)
                .append("。共检查 ")
                .append(evidences.size())
                .append(" 个指标，未通过指标 ")
                .append(failedCount)
                .append(" 个");
        if (concessionCount > 0) {
            explanation.append("，其中 ").append(concessionCount).append(" 个指标落入让步范围");
        }
        if (conflictCount > 0) {
            explanation.append("，并记录 ").append(conflictCount).append(" 条标准差异提示");
        }
        explanation.append("。");

        boolean showAllIndicators = failedCount == 0 && concessionCount == 0;
        List<String> detailLines = new ArrayList<>();
        List<AiSourceReferenceVO> safeCitations = citations == null ? new ArrayList<AiSourceReferenceVO>() : citations;
        for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
            if (evidence == null) {
                continue;
            }
            boolean concession = JudgmentExplainConstants.isConcessionTriggerRule(evidence.getTriggerRule());
            boolean passed = Integer.valueOf(1).equals(evidence.getIsPassed());
            if (!showAllIndicators && passed && !concession) {
                continue;
            }
            StringBuilder line = new StringBuilder();
            line.append("- ")
                    .append(nullToEmpty(evidence.getIndicatorName()))
                    .append(" 实测=").append(formatValue(evidence.getTestValue()))
                    .append(" 下限=").append(formatValue(evidence.getLowerLimit()))
                    .append(" 上限=").append(formatValue(evidence.getUpperLimit()));
            if (evidence.getDeviation() != null) {
                line.append(" 偏差=").append(formatValue(evidence.getDeviation()));
            }
            if (concession) {
                line.append("（落入让步范围）");
            } else if (!passed) {
                line.append("（未通过）");
            }
            int citationIndex = findBestCitationIndex(safeCitations, evidence);
            if (citationIndex > 0) {
                line.append("[").append(citationIndex).append("]");
            }
            detailLines.add(line.toString());
        }
        if (!detailLines.isEmpty()) {
            explanation.append("\n\n指标依据说明：\n");
            for (String detailLine : detailLines) {
                explanation.append(detailLine).append("\n");
            }
        }
        return explanation.toString().trim();
    }

    public static int findBestCitationIndex(List<AiSourceReferenceVO> citations,
                                            QcJudgmentResultVO.EvidenceVO evidence) {
        if (citations == null || citations.isEmpty() || evidence == null) {
            return -1;
        }
        int bestIndex = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < citations.size(); i++) {
            AiSourceReferenceVO citation = citations.get(i);
            if (!referenceSupportsEvidence(citation, evidence)) {
                continue;
            }
            int score = scoreReference(citation, Collections.singletonList(evidence));
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i + 1;
            }
        }
        return bestIndex;
    }

    public static boolean referenceSupportsEvidence(AiSourceReferenceVO reference,
                                                    QcJudgmentResultVO.EvidenceVO evidence) {
        if (reference == null || evidence == null) {
            return false;
        }
        String paragraph = nullToEmpty(reference.getParagraphText());
        if (!StringUtils.hasText(paragraph) || isNonActionableCitationClause(paragraph)) {
            return false;
        }
        if (paragraphContainsLimit(paragraph, evidence.getLowerLimit())
                || paragraphContainsLimit(paragraph, evidence.getUpperLimit())) {
            return true;
        }
        if (JudgmentExplainConstants.isConcessionTriggerRule(evidence.getTriggerRule())
                && isConcessionPolicyClause(paragraph)) {
            if (StringUtils.hasText(evidence.getIndicatorName()) && paragraph.contains(evidence.getIndicatorName())) {
                return true;
            }
            if (StringUtils.hasText(evidence.getIndicatorCode())
                    && paragraph.toUpperCase().contains(evidence.getIndicatorCode().toUpperCase())) {
                return true;
            }
        }
        return StringUtils.hasText(evidence.getIndicatorName())
                && paragraph.contains(evidence.getIndicatorName())
                && paragraphHasRequirementLimits(paragraph);
    }

    public static boolean isConcessionPolicyClause(String paragraphText) {
        if (!StringUtils.hasText(paragraphText)) {
            return false;
        }
        String text = paragraphText.trim();
        return text.contains("让步")
                && (text.contains("接收") || text.contains("评审") || text.contains("偏差") || text.contains("可让步"));
    }

    public static boolean isGroundedCitedAnswer(String content,
                                               List<AiSourceReferenceVO> citations,
                                               List<QcJudgmentResultVO.EvidenceVO> evidences) {
        if (!StringUtils.hasText(content) || citations == null || citations.isEmpty()) {
            return StringUtils.hasText(content) && (citations == null || citations.isEmpty());
        }
        if (!hasCitationMarker(content, citations)) {
            return false;
        }
        Matcher matcher = CITATION_MARKER.matcher(content);
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index < 1 || index > citations.size()) {
                continue;
            }
            AiSourceReferenceVO reference = citations.get(index - 1);
            String context = extractCitationContext(content, matcher.start());
            if (!contextMentionsNumericLimits(context, evidences)) {
                continue;
            }
            if (!referenceSupportsNumericContext(reference, context, evidences)) {
                return false;
            }
        }
        return true;
    }

    public static AiSourceReferenceVO fromClause(StandardClauseVO clause) {
        if (clause == null) {
            return null;
        }
        AiSourceReferenceVO ref = new AiSourceReferenceVO();
        ref.setClauseId(clause.getId());
        ref.setDocumentId(clause.getDocumentId());
        ref.setSourceType(clause.getSourceType());
        ref.setStandardCode(clause.getStandardCode());
        ref.setStandardName(clause.getStandardName());
        ref.setVersionNo(clause.getVersionNo());
        ref.setClauseNo(clause.getClauseNo());
        ref.setPageNo(clause.getPageNo());
        ref.setParagraphText(clause.getParagraphText());
        ref.setIndicatorCode(clause.getIndicatorCode());
        ref.setIndicatorName(clause.getIndicatorName());
        ref.setScore(1.0D);
        return ref;
    }

    public static AiSourceReferenceVO fromRagSource(StandardRagSourceVO source) {
        if (source == null) {
            return null;
        }
        AiSourceReferenceVO reference = new AiSourceReferenceVO();
        reference.setClauseId(source.getClauseId());
        reference.setDocumentId(source.getDocumentId());
        reference.setSourceType(source.getSourceType());
        reference.setStandardCode(source.getStandardCode());
        reference.setStandardName(source.getStandardName());
        reference.setVersionNo(source.getVersionNo());
        reference.setClauseNo(source.getClauseNo());
        reference.setPageNo(source.getPageNo());
        reference.setParagraphText(source.getParagraphText());
        reference.setScore(source.getScore());
        return reference;
    }

    public static void mergeByClauseId(Map<String, AiSourceReferenceVO> target,
                                       Collection<AiSourceReferenceVO> toAdd) {
        if (target == null || toAdd == null) {
            return;
        }
        for (AiSourceReferenceVO citation : toAdd) {
            if (citation != null && StringUtils.hasText(citation.getClauseId())) {
                target.putIfAbsent(citation.getClauseId(), citation);
            }
        }
    }

    public static List<AiSourceReferenceVO> toAiReferences(List<StandardRagSourceVO> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        List<AiSourceReferenceVO> references = new ArrayList<>();
        for (StandardRagSourceVO source : sources) {
            AiSourceReferenceVO reference = fromRagSource(source);
            if (reference != null) {
                references.add(reference);
            }
        }
        return references;
    }

    public static void appendNumberedCitationBlock(StringBuilder prompt, List<AiSourceReferenceVO> citations) {
        appendNumberedCitationBlock(prompt, citations, null);
    }

    public static void appendNumberedCitationBlock(StringBuilder prompt,
                                                   List<AiSourceReferenceVO> citations,
                                                   Function<AiSourceReferenceVO, String> extraLabel) {
        if (prompt == null || citations == null || citations.isEmpty()) {
            return;
        }
        prompt.append("\n来源条款：\n");
        for (int i = 0; i < citations.size(); i++) {
            AiSourceReferenceVO ref = citations.get(i);
            prompt.append("[").append(i + 1).append("] ")
                    .append(nullToEmpty(ref.getStandardCode()))
                    .append(" ")
                    .append(nullToEmpty(ref.getClauseNo()));
            if (extraLabel != null) {
                String extra = extraLabel.apply(ref);
                if (StringUtils.hasText(extra)) {
                    prompt.append(" (").append(extra).append(")");
                }
            }
            prompt.append("：")
                    .append(nullToEmpty(ref.getParagraphText()))
                    .append("\n");
        }
    }

    public static void appendNumberedRagCitationBlock(StringBuilder prompt, List<StandardRagSourceVO> sources) {
        if (prompt == null || sources == null || sources.isEmpty()) {
            return;
        }
        prompt.append("\n来源条款：\n");
        for (int i = 0; i < sources.size(); i++) {
            StandardRagSourceVO source = sources.get(i);
            prompt.append("[").append(i + 1).append("] ")
                    .append(nullToEmpty(source.getStandardCode()))
                    .append(" ")
                    .append(nullToEmpty(source.getClauseNo()));
            if (StringUtils.hasText(source.getSourceFileName())) {
                prompt.append(" (").append(source.getSourceFileName()).append(")");
            }
            prompt.append("：")
                    .append(nullToEmpty(source.getParagraphText()))
                    .append("\n");
        }
    }

    public static void appendCitationAnswerRules(StringBuilder prompt, CitationPromptStyle style) {
        if (prompt == null || style == null) {
            return;
        }
        switch (style) {
            case JUDGMENT_EXPLANATION:
                prompt.append("\n请只依据规则解释和来源条款说明判定原因，必须在关键句后标注来源编号如[1]。");
                prompt.append("仅当来源段落正文中出现该指标的数值、单位或上下限时，才可引用该编号；");
                prompt.append("对可让步(CAN_CONCESSION)判定，须逐条说明落入让步带的指标及实测/限值，并引用写明让步条件的条款；");
                prompt.append("「范围」「适用范围」等章节若未写出具体限值，不得用于指标上下限引用；");
                prompt.append("「术语」「定义」等章节仅解释指标含义，不得用于引用合格限值。");
                break;
            case CERT_QA:
                prompt.append("\n请只依据上述事实回答用户问题，必须在关键句后标注来源编号如[1]。");
                prompt.append("指标上下限若来源段落未写出具体数值，请写「限值见指标依据」且不要错误引用范围章节；");
                prompt.append("仅当来源段落正文中出现该指标的数值、单位或上下限时，才可引用该编号；");
                prompt.append("「术语」「定义」等章节仅解释指标含义，不得用于引用合格限值。");
                prompt.append("不得编造标准、限值、条款或案例；信息不足时必须明确说明。");
                break;
            case STANDARD_RAG:
                prompt.append("\n请只依据上述来源条款回答问题，必须在关键句后标注来源编号如[1]。");
                prompt.append("仅当来源段落正文中出现具体数值、单位或上下限时，才可引用该编号说明限值；");
                prompt.append("「范围」「适用范围」等章节若未写出具体限值，不得用于指标上下限引用；");
                prompt.append("「术语」「定义」等章节仅解释指标含义，不得用于引用合格限值。");
                prompt.append("缺少依据时必须说明没有依据，不得编造标准、限值或案例。");
                break;
            default:
                break;
        }
    }

    public static List<String> buildIndicatorKeywords(QcJudgmentResultVO.EvidenceVO evidence) {
        List<String> keywords = new ArrayList<>();
        if (evidence == null) {
            return keywords;
        }
        if (StringUtils.hasText(evidence.getIndicatorName())) {
            keywords.add(evidence.getIndicatorName());
        }
        if (StringUtils.hasText(evidence.getIndicatorCode())) {
            keywords.add(evidence.getIndicatorCode());
        }
        if (evidence.getLowerLimit() != null) {
            keywords.add(normalizeNumber(evidence.getLowerLimit()));
        }
        if (evidence.getUpperLimit() != null) {
            keywords.add(normalizeNumber(evidence.getUpperLimit()));
        }
        return keywords.stream().filter(StringUtils::hasText).distinct().collect(Collectors.toList());
    }

    private static boolean hasCitationMarker(String content, List<AiSourceReferenceVO> citations) {
        for (int i = 0; i < citations.size(); i++) {
            if (content.contains("[" + (i + 1) + "]")) {
                return true;
            }
        }
        for (AiSourceReferenceVO reference : citations) {
            if (StringUtils.hasText(reference.getClauseNo()) && content.contains(reference.getClauseNo())) {
                return true;
            }
        }
        return false;
    }

    private static String extractCitationContext(String content, int markerStart) {
        int start = Math.max(0, content.lastIndexOf('。', markerStart));
        if (start < 0) {
            start = Math.max(0, content.lastIndexOf('\n', markerStart));
        }
        if (start < 0) {
            start = Math.max(0, markerStart - 120);
        } else {
            start += 1;
        }
        int end = Math.min(content.length(), markerStart + 40);
        int nextBreak = content.indexOf('。', markerStart);
        if (nextBreak > markerStart && nextBreak < end) {
            end = nextBreak + 1;
        }
        return content.substring(start, end);
    }

    private static boolean contextMentionsNumericLimits(String context,
                                                        List<QcJudgmentResultVO.EvidenceVO> evidences) {
        if (!StringUtils.hasText(context)) {
            return false;
        }
        if (NUMERIC_LIMIT_IN_TEXT.matcher(context).find()) {
            return true;
        }
        if (evidences == null) {
            return false;
        }
        for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
            if (paragraphContainsLimit(context, evidence.getLowerLimit())
                    || paragraphContainsLimit(context, evidence.getUpperLimit())
                    || paragraphContainsLimit(context, evidence.getTestValue())) {
                return true;
            }
            if (JudgmentExplainConstants.isConcessionTriggerRule(evidence.getTriggerRule())
                    && StringUtils.hasText(evidence.getIndicatorName())
                    && context.contains(evidence.getIndicatorName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean referenceSupportsNumericContext(AiSourceReferenceVO reference,
                                                           String context,
                                                           List<QcJudgmentResultVO.EvidenceVO> evidences) {
        String paragraph = reference == null ? "" : nullToEmpty(reference.getParagraphText());
        if (!StringUtils.hasText(paragraph)) {
            return false;
        }
        if (isScopeOnlyClause(paragraph)) {
            return false;
        }
        if (isDefinitionOnlyClause(paragraph)) {
            return false;
        }
        if (evidences != null) {
            for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
                if (evidence == null) {
                    continue;
                }
                boolean contextAboutIndicator = StringUtils.hasText(evidence.getIndicatorName())
                        && context.contains(evidence.getIndicatorName());
                if (!contextAboutIndicator) {
                    continue;
                }
                if (referenceSupportsEvidence(reference, evidence)) {
                    return true;
                }
            }
        }
        String sanitizedContext = stripCitationMarkers(context);
        if (evidences != null) {
            for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
                if (evidence == null) {
                    continue;
                }
                if (!contextMentionsEvidenceLimits(sanitizedContext, evidence)) {
                    continue;
                }
                if (paragraphContainsLimit(paragraph, evidence.getLowerLimit())
                        || paragraphContainsLimit(paragraph, evidence.getUpperLimit())) {
                    return true;
                }
            }
        }
        Matcher matcher = SENTENCE_NUMBER.matcher(sanitizedContext);
        while (matcher.find()) {
            String number = matcher.group();
            if (number.length() < 2 && !number.contains(".")) {
                continue;
            }
            if (paragraphContainsLimit(paragraph, number)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contextMentionsEvidenceLimits(String context, QcJudgmentResultVO.EvidenceVO evidence) {
        if (!StringUtils.hasText(context) || evidence == null) {
            return false;
        }
        return paragraphContainsLimit(context, evidence.getLowerLimit())
                || paragraphContainsLimit(context, evidence.getUpperLimit())
                || paragraphContainsLimit(context, evidence.getTestValue());
    }

    private static String stripCitationMarkers(String context) {
        if (!StringUtils.hasText(context)) {
            return "";
        }
        return CITATION_MARKER.matcher(context).replaceAll("");
    }

    static boolean paragraphContainsLimit(String text, Object limit) {
        if (limit == null) {
            return false;
        }
        String normalized = normalizeNumber(limit);
        return StringUtils.hasText(normalized) && StringUtils.hasText(text) && text.contains(normalized);
    }

    private static String normalizeNumber(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        String raw = value.toString().trim();
        if (raw.endsWith(".0")) {
            return raw.substring(0, raw.length() - 2);
        }
        return raw;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "-";
        }
        return normalizeNumber(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class ScoredReference {
        private final AiSourceReferenceVO reference;
        private final int score;

        private ScoredReference(AiSourceReferenceVO reference, int score) {
            this.reference = reference;
            this.score = score;
        }

        private AiSourceReferenceVO getReference() {
            return reference;
        }

        private int getScore() {
            return score;
        }
    }
}
