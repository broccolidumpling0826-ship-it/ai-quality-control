package com.jhict.quality.engine.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JudgmentInput {

    private String recordId;
    private String customerId;
    private String productVariety;
    private String productGrade;
    private String productSpec;
    private LocalDateTime testTime;
    private List<InspectionValueItem> values;

    @Data
    @Builder
    public static class InspectionValueItem {
        private String indicatorId;
        private BigDecimal testValue;
        private String valueText;
    }
}
