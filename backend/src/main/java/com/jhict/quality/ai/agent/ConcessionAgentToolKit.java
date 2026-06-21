package com.jhict.quality.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.entity.*;
import com.jhict.quality.mapper.*;
import com.jhict.quality.vo.QcJudgmentResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConcessionAgentToolKit {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    @Resource
    private QcJudgmentResultMapper judgmentHistoryMapper;

    public Map<String, Object> getJudgmentContext(String judgmentId) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null) {
            return Collections.singletonMap("error", "判定不存在");
        }
        QcInspectionRecord record = inspectionRecordMapper.selectById(judgment.getRecordId());
        List<QcJudgmentEvidence> evidences = judgmentEvidenceMapper.findByJudgmentId(judgmentId);
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("judgmentType", judgment.getJudgmentType());
        ctx.put("batchNo", record != null ? record.getBatchNo() : null);
        ctx.put("variety", record != null ? record.getProductVariety() : null);
        ctx.put("grade", record != null ? record.getProductGrade() : null);
        ctx.put("customerId", record != null ? record.getCustomerId() : null);
        ctx.put("evidences", evidences.stream().map(this::toEvidenceMap).collect(Collectors.toList()));
        return ctx;
    }

    public Map<String, Object> getStandardLimits(String judgmentId) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null) {
            return Collections.emptyMap();
        }
        List<String> stdIds = parseStandardIds(judgment.getMatchedStandardIds());
        if (stdIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcStandardIndicator> limits = standardIndicatorMapper.selectList(
                new LambdaQueryWrapper<QcStandardIndicator>().in(QcStandardIndicator::getStandardId, stdIds));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("limits", limits);
        return result;
    }

    public Map<String, Object> getHistoricalCases(String judgmentId) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null) {
            return Collections.emptyMap();
        }
        QcInspectionRecord record = inspectionRecordMapper.selectById(judgment.getRecordId());
        if (record == null) {
            return Collections.emptyMap();
        }
        List<QcJudgmentResult> similar = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .eq(QcJudgmentResult::getJudgmentType, "CAN_CONCESSION")
                        .ne(QcJudgmentResult::getId, judgmentId)
                        .orderByDesc(QcJudgmentResult::getJudgmentTime)
                        .last("LIMIT 5"));
        List<Map<String, Object>> cases = new ArrayList<>();
        for (QcJudgmentResult j : similar) {
            QcInspectionRecord r = inspectionRecordMapper.selectById(j.getRecordId());
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("batchNo", r != null ? r.getBatchNo() : null);
            c.put("judgmentType", j.getJudgmentType());
            c.put("judgmentTime", j.getJudgmentTime());
            cases.add(c);
        }
        return Collections.singletonMap("cases", cases);
    }

    public Map<String, Object> getCustomerInfo(String judgmentId) {
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null) {
            return Collections.emptyMap();
        }
        QcInspectionRecord record = inspectionRecordMapper.selectById(judgment.getRecordId());
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("customerId", record != null ? record.getCustomerId() : "通用客户");
        info.put("productSpec", record != null ? record.getProductSpec() : null);
        info.put("usage", "一般结构件/汽车零配件");
        return info;
    }

    private Map<String, Object> toEvidenceMap(QcJudgmentEvidence ev) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("indicatorId", ev.getIndicatorId());
        m.put("testValue", ev.getTestValue());
        m.put("lowerLimit", ev.getLowerLimit());
        m.put("upperLimit", ev.getUpperLimit());
        m.put("deviation", ev.getDeviation());
        m.put("triggerRule", ev.getTriggerRule());
        m.put("passed", ev.getIsPassed());
        return m;
    }

    private List<String> parseStandardIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
