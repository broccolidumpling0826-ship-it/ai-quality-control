package com.jhict.quality.ai.judgment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.ai.conflict.StandardConflictDetector;
import com.jhict.quality.entity.QcJudgmentEvidence;
import com.jhict.quality.entity.StandardGap;
import com.jhict.quality.enums.ConfidenceLevel;
import com.jhict.quality.mapper.QcJudgmentEvidenceMapper;
import com.jhict.quality.mapper.StandardGapMapper;
import com.jhict.quality.vo.QcJudgmentResultVO;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ConfidenceCalculator {

    private static final BigDecimal BOUNDARY_RATIO = new BigDecimal("0.05");

    @Resource
    private StandardConflictDetector conflictDetector;

    @Resource
    private StandardGapMapper standardGapMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    public ConfidenceResult calculate(QcJudgmentResultVO judgmentVo) {
        ConfidenceResult result = new ConfidenceResult();
        boolean hasPendingConflict = conflictDetector.hasPendingConflict(
                judgmentVo.getProductVariety(), judgmentVo.getProductGrade());
        if (hasPendingConflict) {
            result.setLevel(ConfidenceLevel.LOW);
            result.setManualReviewRequired(true);
            result.setReason("存在未裁定标准冲突");
            return result;
        }

        boolean hasGap = hasUnresolvedGap(judgmentVo.getProductVariety(), judgmentVo.getProductGrade());
        boolean nearBoundary = isNearBoundary(judgmentVo.getJudgmentId());

        if (hasGap || nearBoundary) {
            result.setLevel(ConfidenceLevel.MEDIUM);
            result.setManualReviewRequired(nearBoundary);
            result.setReason(hasGap ? "存在标准覆盖缺口" : "偏差接近合格边界");
            return result;
        }

        result.setLevel(ConfidenceLevel.HIGH);
        result.setManualReviewRequired(false);
        result.setReason("无冲突且偏差远离边界");
        return result;
    }

    private boolean hasUnresolvedGap(String variety, String grade) {
        if (!StringUtils.hasText(variety) || !StringUtils.hasText(grade)) {
            return false;
        }
        Long count = standardGapMapper.selectCount(new LambdaQueryWrapper<StandardGap>()
                .eq(StandardGap::getVariety, variety)
                .eq(StandardGap::getGrade, grade)
                .eq(StandardGap::getIsResolved, 0));
        return count != null && count > 0;
    }

    private boolean isNearBoundary(String judgmentId) {
        if (!StringUtils.hasText(judgmentId)) {
            return false;
        }
        List<QcJudgmentEvidence> evidences = judgmentEvidenceMapper.findByJudgmentId(judgmentId);
        for (QcJudgmentEvidence ev : evidences) {
            if (ev.getIsPassed() != null && ev.getIsPassed() == 1) {
                continue;
            }
            if (isNearLimit(ev.getTestValue(), ev.getLowerLimit())
                    || isNearLimit(ev.getTestValue(), ev.getUpperLimit())) {
                return true;
            }
            if (ev.getTriggerRule() != null && ev.getTriggerRule().contains("让步")) {
                return true;
            }
        }
        return false;
    }

    private boolean isNearLimit(BigDecimal value, BigDecimal limit) {
        if (value == null || limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal diff = value.subtract(limit).abs();
        BigDecimal ratio = diff.divide(limit.abs().max(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
        return ratio.compareTo(BOUNDARY_RATIO) <= 0;
    }

    @Data
    public static class ConfidenceResult {
        private ConfidenceLevel level;
        private boolean manualReviewRequired;
        private String reason;
    }
}
