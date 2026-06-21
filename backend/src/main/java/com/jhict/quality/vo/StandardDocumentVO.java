package com.jhict.quality.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StandardDocumentVO {

    private String id;

    private String standardId;

    private String documentCode;

    private String documentName;

    private String documentType;

    private String standardType;

    private String standardCode;

    private String standardName;

    private String versionNo;

    private String customerId;

    private String variety;

    private String grade;

    private String specRange;

    private String usageScope;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String sourceFileName;

    private String sourceFilePath;

    private String parseStatus;

    private String indexStatus;

    private String parseErrorMessage;

    private LocalDateTime indexedAt;

    private String status;

    private String remark;
}
