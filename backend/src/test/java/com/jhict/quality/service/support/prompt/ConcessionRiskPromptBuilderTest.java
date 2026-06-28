package com.jhict.quality.service.support.prompt;

import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.vo.ConcessionRiskAssessmentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcessionRiskPromptBuilderTest {

    private ConcessionRiskPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ConcessionRiskPromptBuilder(PromptRegistryTestSupport.createLoadedRegistry());
    }

    @Test
    void build_shouldMatchLegacyPromptShape() {
        ConcessionRiskAssessmentVO result = new ConcessionRiskAssessmentVO();
        result.setNarrativeExplanation("基线说明");
        result.setRiskLevel("LOW");
        result.setSuggestedConditions(Arrays.asList("加强检验", "限制用途"));

        ModelChatRequest request = builder.build("J-001", result);

        assertEquals("concession-risk-v1", request.getPromptVersion());
        assertEquals("CONCESSION_RISK", request.getBusinessType());
        assertEquals("J-001", request.getBusinessId());
        assertTrue(request.getSystemPrompt().contains("不得改变风险等级"));
        String user = request.getMessages().get(0).getContent();
        assertEquals("基线结论：基线说明\n风险等级：LOW\n建议条件：[加强检验, 限制用途]", user);
    }
}
