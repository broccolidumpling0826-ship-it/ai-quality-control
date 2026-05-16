package com.jhict.quality.engine.matcher;

import com.jhict.quality.engine.model.JudgmentInput;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardIndicatorMapper;

import java.util.List;

/**
 * 标准匹配器抽象基类
 * 子类实现不同优先级的标准匹配逻辑
 */
public abstract class AbstractStandardMatcher {

    /**
     * 根据判定输入匹配适用的质量标准列表
     *
     * @param input      判定输入（包含品种、牌号、客户ID、检验时间等）
     * @param stdMapper  质量标准Mapper
     * @param siMapper   标准指标Mapper
     * @return 匹配到的质量标准列表（按优先级排序，最多返回一个匹配标准）
     */
    public abstract List<QcQualityStandard> matchStandards(
            JudgmentInput input,
            QcQualityStandardMapper stdMapper,
            QcStandardIndicatorMapper siMapper
    );
}
