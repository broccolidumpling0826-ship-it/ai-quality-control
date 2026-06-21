package com.jhict.quality.vo;

import lombok.Data;

import java.util.List;

@Data
public class StandardRagAnswerVO {

    private String query;

    private String answer;

    private Boolean refused;

    private String refusalReason;

    private String confidenceLabel;

    private Double confidenceScore;

    private String degradationSource;

    private String degradationReason;

    private Boolean cacheHit;

    private Boolean embeddingUsed;

    private String retrievalMode;

    private Integer chatPromptSourceCount;

    private List<StandardRagSourceVO> sources;
}
