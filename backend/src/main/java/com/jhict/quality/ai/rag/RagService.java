package com.jhict.quality.ai.rag;

import com.alibaba.fastjson2.JSON;
import com.jhict.quality.ai.client.LlmClient;
import com.jhict.quality.ai.client.LlmRequest;
import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.ai.prompt.PromptTemplate;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardDocumentChunk;
import com.jhict.quality.enums.AiCallSource;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.vo.CitationVO;
import com.jhict.quality.vo.RagQueryResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagService {

    private static final int TOP_K = 5;
    private static final String NOT_FOUND_MSG = "未找到相关信息";

    @Resource
    private RagRetriever ragRetriever;

    @Resource
    private LlmClient llmClient;

    @Resource
    private PromptRegistry promptRegistry;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    public RagQueryResultVO query(String question, String standardId, String standardType) {
        RagQueryResultVO result = new RagQueryResultVO();
        List<QcStandardDocumentChunk> chunks = ragRetriever.retrieve(question, standardId, standardType, TOP_K);
        if (chunks.isEmpty()) {
            result.setFound(false);
            result.setAnswer(NOT_FOUND_MSG);
            result.setCitations(new ArrayList<>());
            result.setDegraded(false);
            return result;
        }

        List<CitationVO> citations = buildCitations(chunks);
        String context = chunks.stream()
                .map(c -> "[" + c.getSectionRef() + "] " + c.getChunkText())
                .collect(Collectors.joining("\n\n"));

        PromptTemplate template = promptRegistry.getActiveTemplate("rag-qa");
        String userPrompt = template != null
                ? template.renderUser("question", question, "context", context)
                : question + "\n" + context;

        LlmRequest request = LlmRequest.builder()
                .callSource(AiCallSource.RAG_QUERY)
                .promptKey("rag-qa")
                .systemPrompt(template != null ? template.getSystem() : null)
                .userPrompt(userPrompt)
                .citationIdsJson(JSON.toJSONString(citations.stream().map(CitationVO::getChunkId).collect(Collectors.toList())))
                .build();

        LlmResponse response = llmClient.chat(request);
        String answer = response.getContent();
        if (!response.isSuccess() || response.isDegraded()) {
            answer = buildFallbackAnswer(chunks);
            result.setDegraded(true);
        } else if (!StringUtils.hasText(answer) || answer.contains(NOT_FOUND_MSG)) {
            result.setFound(false);
            result.setAnswer(NOT_FOUND_MSG);
            result.setCitations(citations);
            result.setDegraded(response.isDegraded());
            result.setAuditLogId(response.getAuditLogId());
            return result;
        }

        result.setFound(true);
        result.setAnswer(answer);
        result.setCitations(citations);
        result.setDegraded(response.isDegraded());
        result.setAuditLogId(response.getAuditLogId());
        return result;
    }

    private List<CitationVO> buildCitations(List<QcStandardDocumentChunk> chunks) {
        Map<String, QcQualityStandard> standardMap = qualityStandardMapper.selectBatchIds(
                chunks.stream().map(QcStandardDocumentChunk::getStandardId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(QcQualityStandard::getId, s -> s, (a, b) -> a));

        List<CitationVO> citations = new ArrayList<>();
        for (QcStandardDocumentChunk chunk : chunks) {
            CitationVO vo = new CitationVO();
            vo.setChunkId(chunk.getId());
            vo.setStandardId(chunk.getStandardId());
            vo.setSectionRef(chunk.getSectionRef());
            String text = chunk.getChunkText();
            vo.setHighlightText(text.length() > 200 ? text.substring(0, 200) + "..." : text);
            QcQualityStandard std = standardMap.get(chunk.getStandardId());
            if (std != null) {
                vo.setStandardName(StringUtils.hasText(std.getStandardName())
                        ? std.getStandardName() : std.getStandardCode());
            }
            citations.add(vo);
        }
        return citations;
    }

    private String buildFallbackAnswer(List<QcStandardDocumentChunk> chunks) {
        StringBuilder sb = new StringBuilder("根据检索到的标准段落：\n");
        for (QcStandardDocumentChunk chunk : chunks) {
            sb.append("- ").append(chunk.getSectionRef()).append(": ");
            String text = chunk.getChunkText();
            sb.append(text.length() > 150 ? text.substring(0, 150) + "..." : text).append("\n");
        }
        return sb.toString().trim();
    }
}
