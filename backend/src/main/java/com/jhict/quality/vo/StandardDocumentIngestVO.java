package com.jhict.quality.vo;

import lombok.Data;

import java.util.List;

@Data
public class StandardDocumentIngestVO {

    private String documentId;

    private String parseStatus;

    private String indexStatus;

    private Integer extractedPageCount;

    private Integer chunkCount;

    private Integer indexedCount;

    private List<String> failedClauseIds;

    private String retrievalMode;

    private String errorCategory;

    private String errorMessage;
}
