package com.jhict.quality.service.support.prompt;

import com.jhict.quality.vo.QcJudgmentResultVO;
import org.springframework.util.StringUtils;

import java.util.List;

final class PromptEvidenceFormatter {

    private PromptEvidenceFormatter() {
    }

    static void appendEvidences(StringBuilder prompt, List<QcJudgmentResultVO.EvidenceVO> evidences) {
        if (evidences == null || evidences.isEmpty()) {
            return;
        }
        prompt.append("\n指标依据：\n");
        for (QcJudgmentResultVO.EvidenceVO evidence : evidences) {
            prompt.append("- ")
                    .append(nullToEmpty(evidence.getIndicatorName()))
                    .append(" 实测=").append(evidence.getTestValue())
                    .append(" 下限=").append(evidence.getLowerLimit())
                    .append(" 上限=").append(evidence.getUpperLimit())
                    .append(" 偏差=").append(evidence.getDeviation())
                    .append(" 规则=").append(nullToEmpty(evidence.getTriggerRule()))
                    .append("\n");
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
