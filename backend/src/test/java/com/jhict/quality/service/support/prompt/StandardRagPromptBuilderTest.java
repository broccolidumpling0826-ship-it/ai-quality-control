package com.jhict.quality.service.support.prompt;

import com.jhict.quality.dto.StandardRagQueryCmd;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.vo.StandardRagSourceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardRagPromptBuilderTest {

    private StandardRagPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new StandardRagPromptBuilder(PromptRegistryTestSupport.createLoadedRegistry());
    }

    @Test
    void build_shouldMatchLegacyPromptShape() {
        StandardRagQueryCmd cmd = new StandardRagQueryCmd();
        cmd.setQuery("Q235B 抗拉强度要求");

        StandardRagSourceVO source = new StandardRagSourceVO();
        source.setClauseId("c1");
        source.setClauseNo("5.1");
        source.setParagraphText("抗拉强度 Rm 的合格范围为 370 MPa 至 510 MPa。");

        StandardRagPromptBuilder.BuiltStandardRagPrompt built = builder.build(cmd, Collections.singletonList(source));
        ModelChatRequest request = built.getRequest();

        assertEquals("standard-rag-p0-v3", request.getPromptVersion());
        assertEquals("STANDARD_RAG", request.getBusinessType());
        String user = request.getMessages().get(0).getContent();
        assertTrue(user.startsWith("问题：Q235B 抗拉强度要求\n\n只能依据以下来源条款回答"));
        assertTrue(user.contains("[1]"));
        assertTrue(user.contains("来源编号如[1]"));
    }
}
