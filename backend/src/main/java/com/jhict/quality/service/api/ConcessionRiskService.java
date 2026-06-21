package com.jhict.quality.service.api;

import com.jhict.quality.dto.ConcessionRiskAssessCmd;
import com.jhict.quality.vo.ConcessionRiskAssessmentVO;

public interface ConcessionRiskService {

    ConcessionRiskAssessmentVO assess(ConcessionRiskAssessCmd cmd);
}
