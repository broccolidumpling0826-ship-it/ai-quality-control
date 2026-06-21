package com.jhict.quality.vo;

import lombok.Data;

@Data
public class StandardRagSourceVO {

    private String clauseId;

    private String documentId;

    private String sourceType;

    private String standardCode;

    private String standardName;

    private String versionNo;

    private String clauseNo;

    private Integer pageNo;

    private String paragraphText;

    private Double score;

    private Boolean referenceOnly;
}
