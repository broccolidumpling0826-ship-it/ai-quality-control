package com.jhict.quality.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StandardConflictCreateCmd {

    private String judgmentId;
    private String recordId;
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
