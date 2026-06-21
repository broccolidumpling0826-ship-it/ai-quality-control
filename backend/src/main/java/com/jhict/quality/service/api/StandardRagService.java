package com.jhict.quality.service.api;

import com.jhict.quality.dto.StandardRagQueryCmd;
import com.jhict.quality.vo.StandardRagAnswerVO;

/**
 * Source-grounded standard RAG retrieval service.
 */
public interface StandardRagService {

    /**
     * Answer a natural-language standard query with cited clauses or a refusal/degraded state.
     *
     * @param cmd query command
     * @return cited RAG answer
     */
    StandardRagAnswerVO query(StandardRagQueryCmd cmd);
}
