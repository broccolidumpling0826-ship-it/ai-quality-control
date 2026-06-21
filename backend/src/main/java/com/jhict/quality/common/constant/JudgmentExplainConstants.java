package com.jhict.quality.common.constant;

import com.jhict.quality.enums.StandardType;

/**
 * 判定解释相关常量（标准匹配顺序、指标结论码等）
 */
public final class JudgmentExplainConstants {

    /** 标准优先级匹配顺序：客户协议 → 企标 → 国标 */
    public static final StandardType[] STANDARD_MATCH_PRIORITY = {
            StandardType.CUSTOMER,
            StandardType.ENTERPRISE,
            StandardType.NATIONAL
    };

    public static final String SKIP_REASON_NOT_MATCHED = "未命中该层级标准";

    public static final String INDICATOR_RESULT_PASS = "PASS";

    public static final String INDICATOR_RESULT_FAIL = "FAIL";

    /** 超出合格限但在让步范围内 */
    public static final String INDICATOR_RESULT_CONCESSION = "CONCESSION";

    public static final String INDICATOR_RESULT_WARNING = "WARNING";

    /** 触发规则中含此片段表示指标在让步范围内 */
    public static final String TRIGGER_RULE_CONCESSION_MARKER = "让步范围内";

    public static final int PASSED_FLAG = 1;

    /**
     * 判断触发规则是否表示指标处于让步范围。
     * 兼容引擎文案（含「让步范围内」）、演示种子（含 CAN_CONCESSION / 让步…范围内）等格式。
     */
    public static boolean isConcessionTriggerRule(String triggerRule) {
        if (triggerRule == null || triggerRule.trim().isEmpty()) {
            return false;
        }
        if (triggerRule.contains(TRIGGER_RULE_CONCESSION_MARKER)) {
            return true;
        }
        if (triggerRule.contains("CAN_CONCESSION")) {
            return true;
        }
        return triggerRule.contains("让步") && triggerRule.contains("范围内");
    }

    private JudgmentExplainConstants() {
    }
}
