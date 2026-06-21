package com.jhict.quality.engine.standard;

import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardIndicator;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标准冲突检测的确定性比较工具。
 * 只做结构化规则比较，不读取数据库，不修改判定结论。
 */
public final class StandardCompareUtils {

    public static final String CONFLICT_NUMERIC_LIMIT = "NUMERIC_LIMIT";
    public static final String CONFLICT_UNIT_MISMATCH = "UNIT_MISMATCH";
    public static final String CONFLICT_INDICATOR_SCOPE = "INDICATOR_SCOPE";
    public static final String CONFLICT_SPEC_RANGE = "SPEC_RANGE";

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();

    static {
        UNIT_ALIASES.put("mpa", "MPA");
        UNIT_ALIASES.put("兆帕", "MPA");
        UNIT_ALIASES.put("%", "PERCENT");
        UNIT_ALIASES.put("percent", "PERCENT");
        UNIT_ALIASES.put("百分比", "PERCENT");
        UNIT_ALIASES.put("mm", "MM");
        UNIT_ALIASES.put("毫米", "MM");
        UNIT_ALIASES.put("n/a", "NONE");
        UNIT_ALIASES.put("无", "NONE");
    }

    private StandardCompareUtils() {
    }

    public static RuleCompatibility compareRules(QcQualityStandard leftStandard,
                                                 QcStandardIndicator leftRule,
                                                 QcIndicatorItem leftIndicator,
                                                 QcQualityStandard rightStandard,
                                                 QcStandardIndicator rightRule,
                                                 QcIndicatorItem rightIndicator) {
        if (!comparableIndicator(leftRule, leftIndicator, rightRule, rightIndicator)) {
            return RuleCompatibility.builder()
                    .indicatorComparable(false)
                    .unitComparable(false)
                    .specOverlap(false)
                    .numericLimitsEqual(false)
                    .blockingConflict(true)
                    .conflictType(CONFLICT_INDICATOR_SCOPE)
                    .message("同名或同代码指标口径不一致")
                    .build();
        }

        boolean unitComparable = comparableUnit(unitOf(leftIndicator), unitOf(rightIndicator));
        if (!unitComparable) {
            return RuleCompatibility.builder()
                    .indicatorComparable(true)
                    .unitComparable(false)
                    .specOverlap(false)
                    .numericLimitsEqual(false)
                    .blockingConflict(true)
                    .conflictType(CONFLICT_UNIT_MISMATCH)
                    .message("指标单位不一致且未配置可比别名或换算规则")
                    .build();
        }

        boolean specOverlap = specRangesOverlap(specOf(leftStandard), specOf(rightStandard));
        if (!specOverlap) {
            return RuleCompatibility.builder()
                    .indicatorComparable(true)
                    .unitComparable(true)
                    .specOverlap(false)
                    .numericLimitsEqual(true)
                    .blockingConflict(false)
                    .conflictType(CONFLICT_SPEC_RANGE)
                    .message("规格范围不重叠，规则不可判定为冲突")
                    .build();
        }

        boolean limitsEqual = numericLimitsEqual(leftRule, rightRule);
        return RuleCompatibility.builder()
                .indicatorComparable(true)
                .unitComparable(true)
                .specOverlap(true)
                .numericLimitsEqual(limitsEqual)
                .blockingConflict(!limitsEqual)
                .conflictType(limitsEqual ? null : CONFLICT_NUMERIC_LIMIT)
                .message(limitsEqual ? "指标限值一致" : "同优先级候选标准存在不同上下限")
                .build();
    }

    public static boolean comparableIndicator(QcStandardIndicator leftRule,
                                              QcIndicatorItem leftIndicator,
                                              QcStandardIndicator rightRule,
                                              QcIndicatorItem rightIndicator) {
        if (leftRule != null && rightRule != null
                && StringUtils.hasText(leftRule.getIndicatorId())
                && leftRule.getIndicatorId().equals(rightRule.getIndicatorId())) {
            return true;
        }
        String leftCode = normalizeText(leftIndicator == null ? null : leftIndicator.getIndicatorCode());
        String rightCode = normalizeText(rightIndicator == null ? null : rightIndicator.getIndicatorCode());
        if (StringUtils.hasText(leftCode) && leftCode.equals(rightCode)) {
            return true;
        }
        String leftName = normalizeText(leftIndicator == null ? null : leftIndicator.getIndicatorName());
        String rightName = normalizeText(rightIndicator == null ? null : rightIndicator.getIndicatorName());
        return StringUtils.hasText(leftName) && leftName.equals(rightName);
    }

    public static boolean comparableUnit(String leftUnit, String rightUnit) {
        return normalizeUnit(leftUnit).equals(normalizeUnit(rightUnit));
    }

    public static boolean numericLimitsEqual(QcStandardIndicator leftRule, QcStandardIndicator rightRule) {
        if (leftRule == null || rightRule == null) {
            return false;
        }
        return decimalEquals(leftRule.getLowerLimit(), rightRule.getLowerLimit())
                && decimalEquals(leftRule.getUpperLimit(), rightRule.getUpperLimit());
    }

    public static boolean specRangesOverlap(String leftSpecRange, String rightSpecRange) {
        SpecRange left = parseSpecRange(leftSpecRange);
        SpecRange right = parseSpecRange(rightSpecRange);
        if (left.isUnparseable() || right.isUnparseable()) {
            return true;
        }
        if (left.getUpper() != null && right.getLower() != null
                && left.getUpper().compareTo(right.getLower()) < 0) {
            return false;
        }
        if (right.getUpper() != null && left.getLower() != null
                && right.getUpper().compareTo(left.getLower()) < 0) {
            return false;
        }
        return true;
    }

    public static SpecRange parseSpecRange(String raw) {
        if (!StringUtils.hasText(raw)) {
            return SpecRange.builder().raw(raw).unparseable(false).build();
        }
        String normalized = raw.trim()
                .replace("（", "(")
                .replace("）", ")")
                .replace("，", ",")
                .replace("～", "-")
                .replace("—", "-")
                .replace("－", "-");
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (lowerCase.contains("全") || lowerCase.contains("all")) {
            return SpecRange.builder().raw(raw).unparseable(false).build();
        }

        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        BigDecimal first = null;
        BigDecimal second = null;
        if (matcher.find()) {
            first = new BigDecimal(matcher.group());
        }
        if (matcher.find()) {
            second = new BigDecimal(matcher.group());
        }
        if (first == null) {
            return SpecRange.builder().raw(raw).unparseable(true).build();
        }
        if (second != null) {
            BigDecimal lower = first.min(second);
            BigDecimal upper = first.max(second);
            return SpecRange.builder().raw(raw).lower(lower).upper(upper).unparseable(false).build();
        }
        if (normalized.contains(">") || normalized.contains("≥") || lowerCase.contains("gte")) {
            return SpecRange.builder().raw(raw).lower(first).unparseable(false).build();
        }
        if (normalized.contains("<") || normalized.contains("≤") || lowerCase.contains("lte")) {
            return SpecRange.builder().raw(raw).upper(first).unparseable(false).build();
        }
        return SpecRange.builder().raw(raw).lower(first).upper(first).unparseable(false).build();
    }

    public static String normalizeUnit(String unit) {
        String normalized = normalizeText(unit);
        if (!StringUtils.hasText(normalized)) {
            return "NONE";
        }
        String alias = UNIT_ALIASES.get(normalized.toLowerCase(Locale.ROOT));
        return alias != null ? alias : normalized.toUpperCase(Locale.ROOT);
    }

    public static String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.trim()
                .replace(" ", "")
                .replace("　", "")
                .toLowerCase(Locale.ROOT);
    }

    private static boolean decimalEquals(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    private static String unitOf(QcIndicatorItem item) {
        return item == null ? null : item.getUnit();
    }

    private static String specOf(QcQualityStandard standard) {
        return standard == null ? null : standard.getSpecRange();
    }

    @Data
    @Builder
    public static class RuleCompatibility {
        private boolean indicatorComparable;
        private boolean unitComparable;
        private boolean specOverlap;
        private boolean numericLimitsEqual;
        private boolean blockingConflict;
        private String conflictType;
        private String message;
    }

    @Data
    @Builder
    public static class SpecRange {
        private String raw;
        private BigDecimal lower;
        private BigDecimal upper;
        private boolean unparseable;
    }
}
