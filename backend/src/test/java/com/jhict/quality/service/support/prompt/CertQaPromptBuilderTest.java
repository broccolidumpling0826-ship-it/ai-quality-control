package com.jhict.quality.service.support.prompt;

import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.QcQualityCertDataVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertQaPromptBuilderTest {

    private CertQaPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new CertQaPromptBuilder(PromptRegistryTestSupport.createLoadedRegistry());
    }

    @Test
    void build_shouldAppendNonFinalGuidance() {
        CertQaQueryCmd cmd = new CertQaQueryCmd();
        cmd.setQuestion("能否出具质保书？");

        QcJudgmentResultVO judgment = new QcJudgmentResultVO();
        judgment.setJudgmentId("J-001");
        judgment.setJudgmentType(JudgmentType.QUALIFIED.getCode());

        ModelChatRequest request = builder.build(cmd, null, judgment, Collections.emptyList(),
                Collections.emptyList(), "规则摘要", false, true, false);

        String user = request.getMessages().get(0).getContent();
        assertTrue(user.contains("质保书快照状态：未生成"));
        assertTrue(user.contains("是否非最终态：是"));
        assertTrue(user.contains("不可正式出证或非最终状态"));
    }

    @Test
    void build_shouldAppendConcessionApprovedSnapshotGuidance() {
        CertQaQueryCmd cmd = new CertQaQueryCmd();
        cmd.setQuestion("能否导出 PDF？");

        QcJudgmentResultVO judgment = new QcJudgmentResultVO();
        judgment.setJudgmentId("J-002");
        judgment.setJudgmentType(JudgmentType.CAN_CONCESSION.getCode());

        ModelChatRequest request = builder.build(cmd, null, judgment, Collections.emptyList(),
                Collections.emptyList(), "规则摘要", true, true, true);

        String user = request.getMessages().get(0).getContent();
        assertTrue(user.contains("让步接收审批：已通过"));
        assertTrue(user.contains("仅缺质保书数据快照"));
        assertEquals("cert-qa-v4", request.getPromptVersion());
    }
}
