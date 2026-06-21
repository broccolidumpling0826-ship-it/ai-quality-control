package com.jhict.quality.service.support.rag;

import lombok.Data;

@Data
public class StandardClauseChunk {

    private String clauseNo;

    private String clauseTitle;

    private Integer pageNo;

    private String paragraphText;

    private String retrievalKeywords;
}
