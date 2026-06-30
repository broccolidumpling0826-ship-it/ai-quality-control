package com.jhict.quality.service.support.ai;

import com.jhict.quality.vo.AiSourceReferenceVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiFallbackCacheContext {

    private String assessmentType;
    private String businessType;
    private String businessId;
    private String promptVersion;
    private String modelName;
    private Map<String, Object> inputSnapshot;
    private String outputText;
    private String structuredOutput;
    private List<AiSourceReferenceVO> references;
    private String confidenceLabel;
    private BigDecimal confidenceScore;
}
