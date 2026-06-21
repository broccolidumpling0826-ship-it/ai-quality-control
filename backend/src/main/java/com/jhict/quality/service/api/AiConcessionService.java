package com.jhict.quality.service.api;

import com.jhict.quality.dto.ConcessionAssessCmd;
import com.jhict.quality.vo.ConcessionAssessmentVO;

public interface AiConcessionService {

    ConcessionAssessmentVO assess(ConcessionAssessCmd cmd);
}
