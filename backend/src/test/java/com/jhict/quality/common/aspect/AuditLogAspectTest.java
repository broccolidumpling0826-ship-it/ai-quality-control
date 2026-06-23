package com.jhict.quality.common.aspect;

import com.jhict.quality.common.aspect.AuditLogAspect.AuditLogWriter;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.entity.QcAiAssessment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogAspectTest {

    @Test
    void resolveTargetId_prefersReturnedEntityId() {
        AiAssessmentCreateCmd cmd = new AiAssessmentCreateCmd();
        cmd.setBusinessId("FT-CONC-001");
        cmd.setRelatedJudgmentId("judgment-123");

        QcAiAssessment created = new QcAiAssessment();
        created.setId("assessment-999");

        assertEquals("assessment-999",
                AuditLogWriter.resolveTargetId(new Object[]{cmd}, created));
    }

    @Test
    void resolveTargetId_fallsBackToBusinessIdWhenNoResultId() {
        AiAssessmentCreateCmd cmd = new AiAssessmentCreateCmd();
        cmd.setBusinessId("FT-CONC-001");

        assertEquals("FT-CONC-001",
                AuditLogWriter.resolveTargetId(new Object[]{cmd}, null));
    }
}
