package com.jhict.quality.engine.model;

import com.jhict.quality.enums.JudgmentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class JudgmentOutput {

    private JudgmentType judgmentType;
    private List<EvidenceItem> evidences;
    private List<StandardGapItem> gaps;
    private List<String> matchedStandardIds;
    private List<StandardConflictItem> conflicts;

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

    @Data
    @Builder
    public static class StandardConflictItem {
        private String conflictType;
        private String conflictLevel;
        private String status;
        private String indicatorId;
        private String indicatorName;
        private String unit;
        private String customerId;
        private String variety;
        private String grade;
        private String productSpec;
        private LocalDate inspectionDate;
        private String selectedStandardId;
        private List<String> involvedStandardIds;
        private String conflictDetail;
        private String selectedPriority;
    }
}
