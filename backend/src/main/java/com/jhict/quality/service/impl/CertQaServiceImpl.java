package com.jhict.quality.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.service.api.AiCacheService;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.service.api.CertQaService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.vo.CertQaAnswerVO;
import com.jhict.quality.vo.QcJudgmentListVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.QcQualityCertDataVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CertQaServiceImpl implements CertQaService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private AiCacheService aiCacheService;

    @Resource
    private CertDataService certDataService;

    @Resource
    private JudgmentService judgmentService;

    @Override
    public CertQaAnswerVO answer(CertQaQueryCmd cmd) {
        CertQaAnswerVO answer = initAnswer();
        if (!StringUtils.hasText(cmd.getCoilNo()) && !StringUtils.hasText(cmd.getBatchNo())) {
            return refuse(answer, "必须提供卷号或批次号");
        }
        CertQaAnswerVO cached = tryCache(cmd);
        if (cached != null) {
            return cached;
        }

        QcQualityCertDataVO cert = loadLatestCert(cmd);
        answer.setCertificateSnapshotFound(cert != null);
        QcJudgmentResultVO judgment = loadFinalJudgment(cmd);
        if (judgment == null) {
            return refuse(answer, "未找到卷号/批次对应的最终判定");
        }

        answer.setCitations(judgment.getCitations() == null ? Collections.emptyList() : judgment.getCitations());
        answer.setIndicatorBasis(resolveIndicatorBasis(cmd.getQuestion(), judgment));
        applyStateGate(answer, cert, judgment);
        if (Boolean.TRUE.equals(answer.getRefused())) {
            return answer;
        }

        answer.setAnswer(buildAnswerText(cmd.getQuestion(), cert, judgment, answer.getIndicatorBasis()));
        if (answer.getCitations().isEmpty()) {
            answer.setConfidenceLabel("MEDIUM");
            answer.setDegradationSource("RULE_TEMPLATE");
            answer.setAnswer(answer.getAnswer() + " 未找到可引用的来源段落，以上仅基于结构化判定快照。");
        } else {
            answer.setConfidenceLabel("HIGH");
            answer.setDegradationSource("RAW_RETRIEVAL");
        }
        return answer;
    }

    private CertQaAnswerVO tryCache(CertQaQueryCmd cmd) {
        QcAiCache cache = null;
        if (StringUtils.hasText(cmd.getCoilNo())) {
            cache = aiCacheService.findActive("CERT_QA", "COIL", cmd.getCoilNo());
        }
        if (cache == null && StringUtils.hasText(cmd.getBatchNo())) {
            cache = aiCacheService.findActive("CERT_QA", "BATCH", cmd.getBatchNo());
        }
        if (cache == null) {
            return null;
        }
        CertQaAnswerVO answer = initAnswer();
        answer.setCacheHit(true);
        answer.setConfidenceLabel(cache.getConfidenceLabel());
        answer.setDegradationSource("CACHE");
        answer.setCitations(parseCacheCitations(cache.getReferencesJson()));
        answer.setAnswer(parseCacheAnswer(cache.getCachedOutput()));
        answer.setNonFinal(answer.getAnswer() != null
                && (answer.getAnswer().contains("不能生成正式质保书") || answer.getAnswer().contains("预览")));
        return answer;
    }

    private String parseCacheAnswer(String cachedOutput) {
        try {
            Map<String, Object> map = objectMapper.readValue(cachedOutput, new TypeReference<Map<String, Object>>() {
            });
            Object answer = map.get("answer");
            Object summary = map.get("summary");
            return answer != null ? answer.toString() : (summary == null ? cachedOutput : summary.toString());
        } catch (Exception e) {
            return cachedOutput;
        }
    }

    @SuppressWarnings("unchecked")
    private List<com.jhict.quality.vo.AiSourceReferenceVO> parseCacheCitations(String referencesJson) {
        if (!StringUtils.hasText(referencesJson)) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(referencesJson, new TypeReference<Map<String, Object>>() {
            });
            Object citations = map.get("citations");
            if (!(citations instanceof List)) {
                return Collections.emptyList();
            }
            List<com.jhict.quality.vo.AiSourceReferenceVO> refs = new ArrayList<>();
            for (Object item : (List<Object>) citations) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> citation = (Map<String, Object>) item;
                com.jhict.quality.vo.AiSourceReferenceVO ref = new com.jhict.quality.vo.AiSourceReferenceVO();
                ref.setClauseId(String.valueOf(citation.get("clauseId")));
                ref.setStandardCode(String.valueOf(citation.get("standardCode")));
                ref.setClauseNo(String.valueOf(citation.get("clauseNo")));
                ref.setScore(1.0D);
                refs.add(ref);
            }
            return refs;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private CertQaAnswerVO initAnswer() {
        CertQaAnswerVO answer = new CertQaAnswerVO();
        answer.setRefused(false);
        answer.setNonFinal(false);
        answer.setCertificateSnapshotFound(false);
        answer.setCacheHit(false);
        answer.setCitations(new ArrayList<>());
        answer.setIndicatorBasis(new ArrayList<>());
        answer.setConfidenceLabel("MEDIUM");
        answer.setDegradationSource("RULE_TEMPLATE");
        return answer;
    }

    private QcQualityCertDataVO loadLatestCert(CertQaQueryCmd cmd) {
        IPage<QcQualityCertDataVO> page = certDataService.page(1, 1, cmd.getCoilNo(), cmd.getBatchNo(), null, null);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    private QcJudgmentResultVO loadFinalJudgment(CertQaQueryCmd cmd) {
        QcJudgmentPageQuery query = new QcJudgmentPageQuery();
        query.setCoilNo(cmd.getCoilNo());
        query.setBatchNo(cmd.getBatchNo());
        query.setIsFinal(1);
        query.setPageNum(1);
        query.setPageSize(1);
        IPage<QcJudgmentListVO> page = judgmentService.page(query);
        if (page.getRecords().isEmpty()) {
            return null;
        }
        return judgmentService.getExplanationSnapshot(page.getRecords().get(0).getId());
    }

    private void applyStateGate(CertQaAnswerVO answer, QcQualityCertDataVO cert, QcJudgmentResultVO judgment) {
        String type = judgment.getJudgmentType();
        if (JudgmentType.STANDARD_CONFLICT.getCode().equals(type)) {
            answer.setNonFinal(true);
            refuse(answer, "当前最终判定为标准冲突，完成冲突裁决前不能形成正式质保书结论");
            return;
        }
        if (JudgmentType.UNQUALIFIED.getCode().equals(type) || JudgmentType.NEED_REINSPECTION.getCode().equals(type)) {
            answer.setNonFinal(true);
            refuse(answer, "当前判定为 " + type + "，不满足正式质保书放行条件");
            return;
        }
        if (JudgmentType.CAN_CONCESSION.getCode().equals(type)
                && (cert == null || !"SUCCESS".equals(cert.getStatus()))) {
            answer.setNonFinal(true);
            answer.setAnswer("当前为可让步判定，但尚未确认存在正式质保书快照；需完成客户确认和让步审批后才能正式化。");
        }
        if (cert == null) {
            answer.setNonFinal(true);
        }
    }

    private List<QcJudgmentResultVO.EvidenceVO> resolveIndicatorBasis(String question, QcJudgmentResultVO judgment) {
        if (judgment.getEvidences() == null) {
            return Collections.emptyList();
        }
        List<QcJudgmentResultVO.EvidenceVO> matched = judgment.getEvidences().stream()
                .filter(e -> contains(question, e.getIndicatorName()) || contains(question, e.getIndicatorCode()))
                .collect(Collectors.toList());
        return matched.isEmpty() ? judgment.getEvidences() : matched;
    }

    private String buildAnswerText(String question, QcQualityCertDataVO cert, QcJudgmentResultVO judgment,
                                   List<QcJudgmentResultVO.EvidenceVO> basis) {
        StringBuilder sb = new StringBuilder();
        if (cert == null) {
            sb.append("未找到正式质保书快照，以下回答来自检验记录和最终判定快照。");
        } else {
            sb.append("已找到质保书快照，状态=").append(cert.getStatus()).append("。");
        }
        sb.append("当前最终判定为 ").append(judgment.getJudgmentType()).append("。");
        if (!basis.isEmpty()) {
            sb.append("关键指标依据：");
            for (QcJudgmentResultVO.EvidenceVO e : basis) {
                sb.append(nullToEmpty(e.getIndicatorName()))
                        .append(" 实测=").append(e.getTestValue())
                        .append(" 下限=").append(e.getLowerLimit())
                        .append(" 上限=").append(e.getUpperLimit())
                        .append(" 偏差=").append(e.getDeviation())
                        .append(" 规则=").append(nullToEmpty(e.getTriggerRule()))
                        .append("；");
            }
        }
        return sb.toString();
    }

    private CertQaAnswerVO refuse(CertQaAnswerVO answer, String reason) {
        answer.setRefused(true);
        answer.setRefusalReason(reason);
        answer.setAnswer(reason);
        answer.setConfidenceLabel("LOW");
        answer.setDegradationSource("RULE_TEMPLATE");
        return answer;
    }

    private boolean contains(String question, String keyword) {
        return StringUtils.hasText(question) && StringUtils.hasText(keyword) && question.contains(keyword);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
