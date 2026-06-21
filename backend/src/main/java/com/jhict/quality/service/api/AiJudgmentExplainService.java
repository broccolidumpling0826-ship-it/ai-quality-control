package com.jhict.quality.service.api;

import com.jhict.quality.vo.AiJudgmentExplanationVO;

public interface AiJudgmentExplainService {

    AiJudgmentExplanationVO explain(String judgmentId);

    AiJudgmentExplanationVO explainByDemoCode(String demoCode);
}
