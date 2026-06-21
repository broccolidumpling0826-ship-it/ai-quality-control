package com.jhict.quality.ai.conflict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcStandardConflict;
import com.jhict.quality.entity.QcStandardIndicator;
import com.jhict.quality.enums.ConflictStatus;
import com.jhict.quality.mapper.QcStandardConflictMapper;
import com.jhict.quality.mapper.QcStandardIndicatorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StandardConflictDetector {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Resource
    private QcStandardConflictMapper conflictMapper;

    public List<QcStandardConflict> detectAndPersist(QcInspectionRecord record, List<String> matchedStandardIds) {
        if (record == null || matchedStandardIds == null || matchedStandardIds.size() < 2) {
            return new ArrayList<>();
        }
        List<QcStandardIndicator> indicators = standardIndicatorMapper.selectList(
                new LambdaQueryWrapper<QcStandardIndicator>()
                        .in(QcStandardIndicator::getStandardId, matchedStandardIds));

        Map<String, Map<String, QcStandardIndicator>> byIndicator = new HashMap<>();
        for (QcStandardIndicator si : indicators) {
            byIndicator.computeIfAbsent(si.getIndicatorId(), k -> new HashMap<>())
                    .put(si.getStandardId(), si);
        }

        List<QcStandardConflict> detected = new ArrayList<>();
        String now = LocalDateTime.now().format(FORMATTER);

        for (Map.Entry<String, Map<String, QcStandardIndicator>> entry : byIndicator.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            List<QcStandardIndicator> group = new ArrayList<>(entry.getValue().values());
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    QcStandardIndicator a = group.get(i);
                    QcStandardIndicator b = group.get(j);
                    if (!hasConflict(a, b)) {
                        continue;
                    }
                    String stdA = a.getStandardId();
                    String stdB = b.getStandardId();
                    if (stdA.compareTo(stdB) > 0) {
                        QcStandardIndicator tmp = a;
                        a = b;
                        b = tmp;
                        stdA = a.getStandardId();
                        stdB = b.getStandardId();
                    }
                    QcStandardConflict existing = conflictMapper.findExisting(
                            record.getProductVariety(), record.getProductGrade(),
                            entry.getKey(), stdA, stdB);
                    if (existing != null) {
                        detected.add(existing);
                        continue;
                    }
                    QcStandardConflict conflict = new QcStandardConflict();
                    conflict.setVariety(record.getProductVariety());
                    conflict.setGrade(record.getProductGrade());
                    conflict.setIndicatorId(entry.getKey());
                    conflict.setStandardIdA(stdA);
                    conflict.setStandardIdB(stdB);
                    conflict.setLimitAUpper(a.getUpperLimit());
                    conflict.setLimitALower(a.getLowerLimit());
                    conflict.setLimitBUpper(b.getUpperLimit());
                    conflict.setLimitBLower(b.getLowerLimit());
                    conflict.setConflictStatus(ConflictStatus.PENDING.getCode());
                    conflict.setDemoFlag(0);
                    conflict.setCreateDateTime(now);
                    conflict.setUpdateDateTime(now);
                    conflictMapper.insert(conflict);
                    detected.add(conflict);
                    log.info("检测到标准冲突，indicator={}, stdA={}, stdB={}", entry.getKey(), stdA, stdB);
                }
            }
        }
        return detected;
    }

    private boolean hasConflict(QcStandardIndicator a, QcStandardIndicator b) {
        if (limitsEqual(a.getUpperLimit(), b.getUpperLimit())
                && limitsEqual(a.getLowerLimit(), b.getLowerLimit())) {
            return false;
        }
        return !limitsCompatible(a, b);
    }

    private boolean limitsCompatible(QcStandardIndicator a, QcStandardIndicator b) {
        BigDecimal aLow = a.getLowerLimit();
        BigDecimal aHigh = a.getUpperLimit();
        BigDecimal bLow = b.getLowerLimit();
        BigDecimal bHigh = b.getUpperLimit();
        if (aLow != null && bHigh != null && aLow.compareTo(bHigh) > 0) {
            return false;
        }
        if (bLow != null && aHigh != null && bLow.compareTo(aHigh) > 0) {
            return false;
        }
        if (aLow != null && bLow != null && aLow.compareTo(bLow) != 0) {
            return true;
        }
        if (aHigh != null && bHigh != null && aHigh.compareTo(bHigh) != 0) {
            return true;
        }
        return aLow != null && bLow != null && aLow.compareTo(bLow) != 0;
    }

    private boolean limitsEqual(BigDecimal x, BigDecimal y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.compareTo(y) == 0;
    }

    public boolean hasPendingConflict(String variety, String grade) {
        if (!StringUtils.hasText(variety) || !StringUtils.hasText(grade)) {
            return false;
        }
        Long count = conflictMapper.selectCount(new LambdaQueryWrapper<QcStandardConflict>()
                .eq(QcStandardConflict::getVariety, variety)
                .eq(QcStandardConflict::getGrade, grade)
                .eq(QcStandardConflict::getConflictStatus, ConflictStatus.PENDING.getCode()));
        return count != null && count > 0;
    }
}
