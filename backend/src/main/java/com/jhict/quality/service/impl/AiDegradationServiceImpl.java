package com.jhict.quality.service.impl;

import com.jhict.quality.dto.AiDegradationRequest;
import com.jhict.quality.enums.AiDegradationSource;
import com.jhict.quality.service.api.AiDegradationService;
import com.jhict.quality.vo.AiDegradationResultVO;
import com.jhict.quality.vo.AiSourceReferenceVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class AiDegradationServiceImpl implements AiDegradationService {

    private static final String DEFAULT_UNAVAILABLE_REASON = "AI知识服务当前不可用，系统保留结构化业务数据";
    private static final String RAW_RETRIEVAL_PREFIX = "模型生成不可用，以下仅为原始检索到的来源条款，不能作为最终自动结论。";

    @Override
    public AiDegradationResultVO resolveAiOutput(AiDegradationRequest request) {
        AiDegradationRequest safeRequest = request == null ? new AiDegradationRequest() : request;
        if (StringUtils.hasText(safeRequest.getCachedOutput())) {
            return buildResult(safeRequest, safeRequest.getCachedOutput(), AiDegradationSource.CACHE,
                    "命中预生成缓存", true, safeReferences(safeRequest));
        }
        if (StringUtils.hasText(safeRequest.getGeneratedOutput())) {
            return buildResult(safeRequest, safeRequest.getGeneratedOutput(), AiDegradationSource.GENERATED,
                    "模型生成成功", true, safeReferences(safeRequest));
        }
        if (StringUtils.hasText(safeRequest.getRuleTemplateOutput())) {
            return buildResult(safeRequest, safeRequest.getRuleTemplateOutput(), AiDegradationSource.RULE_TEMPLATE,
                    "模型不可用，使用结构化规则模板", true, safeReferences(safeRequest));
        }
        if (!CollectionUtils.isEmpty(safeRequest.getRawReferences())) {
            return buildResult(safeRequest, RAW_RETRIEVAL_PREFIX, AiDegradationSource.RAW_RETRIEVAL,
                    "模型不可用，返回原始检索条款", false, safeReferences(safeRequest));
        }
        return buildResult(safeRequest, "", AiDegradationSource.UNAVAILABLE,
                unavailableReason(safeRequest), false, Collections.emptyList());
    }

    private AiDegradationResultVO buildResult(AiDegradationRequest request, String outputText,
                                              AiDegradationSource source, String reason,
                                              boolean authoritative,
                                              List<AiSourceReferenceVO> references) {
        AiDegradationResultVO result = new AiDegradationResultVO();
        result.setAssessmentType(request.getAssessmentType());
        result.setBusinessType(request.getBusinessType());
        result.setBusinessId(request.getBusinessId());
        result.setCacheKey(request.getCacheKey());
        result.setOutputText(outputText);
        result.setDegradationSource(source.getCode());
        result.setDegradationReason(reason);
        result.setConfidenceLabel(request.getConfidenceLabel());
        result.setConfidenceScore(request.getConfidenceScore());
        result.setAuthoritative(authoritative);
        result.setReferences(references);
        return result;
    }

    private List<AiSourceReferenceVO> safeReferences(AiDegradationRequest request) {
        return request.getRawReferences() == null ? Collections.emptyList() : request.getRawReferences();
    }

    private String unavailableReason(AiDegradationRequest request) {
        return StringUtils.hasText(request.getUnavailableReason())
                ? request.getUnavailableReason()
                : DEFAULT_UNAVAILABLE_REASON;
    }
}
