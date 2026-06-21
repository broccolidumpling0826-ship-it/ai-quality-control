package com.jhict.quality.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JudgmentExplainConstantsTest {

    @Test
    void recognizesEngineConcessionTriggerRule() {
        assertTrue(JudgmentExplainConstants.isConcessionTriggerRule("实测值超出标准范围但在让步范围内，让步下限=360"));
    }

    @Test
    void recognizesDemoSeedConcessionTriggerRule() {
        assertTrue(JudgmentExplainConstants.isConcessionTriggerRule(
                "实测值 365.0 MPa 低于国标下限 370.0 MPa，但在让步下限 360.0 MPa 范围内，触发 CAN_CONCESSION"));
    }

    @Test
    void rejectsUnqualifiedTriggerRule() {
        assertFalse(JudgmentExplainConstants.isConcessionTriggerRule("实测值超出标准范围且超出让步范围，低于下限 20"));
    }
}
