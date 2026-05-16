package com.jhict.quality.engine.model;

import com.jhict.quality.enums.JudgmentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class JudgmentOutput {

    private JudgmentType judgmentType;
    private List<EvidenceItem> evidences;
    private List<StandardGapItem> gaps;
    private List<String> matchedStandardIds;

    @Data
    @Builder
    public static class EvidenceItem {
        private String standardId;
        private String indicatorId;
        private BigDecimal testValue;
        private BigDecimal upperLimit;
        private BigDecimal lowerLimit;
        private BigDecimal deviation;
        private String triggerRule;
        private boolean passed;
    }

    @Data
    @Builder
    public static class StandardGapItem {
        private String indicatorId;
        private String variety;
        private String grade;
    }
}
