package com.jhict.quality.engine.standard;

import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardIndicator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StandardCompareUtilsTest {

    @Test
    void specRangesOverlap_shouldParseCommonNumericRanges() {
        assertTrue(StandardCompareUtils.specRangesOverlap("1.5-3.0mm", "2.0-4.0mm"));
        assertFalse(StandardCompareUtils.specRangesOverlap("1.0-1.4mm", "1.5-2.0mm"));
        assertTrue(StandardCompareUtils.specRangesOverlap("全规格", "1.5-2.0mm"));
        assertTrue(StandardCompareUtils.specRangesOverlap("厚度未解析文本", "1.5-2.0mm"));
    }

    @Test
    void comparableUnit_shouldUseKnownAliases() {
        assertTrue(StandardCompareUtils.comparableUnit("MPa", "兆帕"));
        assertTrue(StandardCompareUtils.comparableUnit("%", "百分比"));
        assertFalse(StandardCompareUtils.comparableUnit("MPa", "%"));
    }

    @Test
    void compareRules_shouldReturnBlockingNumericLimitConflict() {
        QcQualityStandard leftStandard = standard("1.5-3.0mm");
        QcQualityStandard rightStandard = standard("2.0-4.0mm");
        QcStandardIndicator leftRule = rule("ind-rm", "370", "510");
        QcStandardIndicator rightRule = rule("ind-rm", "390", "510");
        QcIndicatorItem indicator = indicator("RM", "Rm", "MPa");

        StandardCompareUtils.RuleCompatibility result = StandardCompareUtils.compareRules(
                leftStandard, leftRule, indicator, rightStandard, rightRule, indicator);

        assertTrue(result.isBlockingConflict());
        assertEquals(StandardCompareUtils.CONFLICT_NUMERIC_LIMIT, result.getConflictType());
    }

    @Test
    void compareRules_shouldNotConflictWhenSpecRangesDoNotOverlap() {
        QcQualityStandard leftStandard = standard("1.0-1.4mm");
        QcQualityStandard rightStandard = standard("1.5-2.0mm");
        QcStandardIndicator leftRule = rule("ind-rm", "370", "510");
        QcStandardIndicator rightRule = rule("ind-rm", "390", "510");
        QcIndicatorItem indicator = indicator("RM", "Rm", "MPa");

        StandardCompareUtils.RuleCompatibility result = StandardCompareUtils.compareRules(
                leftStandard, leftRule, indicator, rightStandard, rightRule, indicator);

        assertFalse(result.isBlockingConflict());
        assertFalse(result.isSpecOverlap());
    }

    private QcQualityStandard standard(String specRange) {
        QcQualityStandard standard = new QcQualityStandard();
        standard.setSpecRange(specRange);
        return standard;
    }

    private QcStandardIndicator rule(String indicatorId, String lower, String upper) {
        QcStandardIndicator rule = new QcStandardIndicator();
        rule.setIndicatorId(indicatorId);
        rule.setLowerLimit(new BigDecimal(lower));
        rule.setUpperLimit(new BigDecimal(upper));
        return rule;
    }

    private QcIndicatorItem indicator(String code, String name, String unit) {
        QcIndicatorItem item = new QcIndicatorItem();
        item.setIndicatorCode(code);
        item.setIndicatorName(name);
        item.setUnit(unit);
        return item;
    }
}
