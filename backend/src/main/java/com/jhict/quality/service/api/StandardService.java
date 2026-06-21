package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcQualityStandardAddCmd;
import com.jhict.quality.dto.QcQualityStandardPageQuery;
import com.jhict.quality.dto.StandardCandidateQuery;
import com.jhict.quality.vo.QcIndicatorItemVO;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.QcQualityStandardVO;
import com.jhict.quality.vo.StandardCandidateSetVO;

import java.util.List;
import java.util.Map;

/**
 * 质量标准服务接口
 */
public interface StandardService {

    /**
     * 新增质量标准
     *
     * @param cmd 新增命令（含指标配置列表）
     * @return 新增标准的ID
     */
    String addStandard(QcQualityStandardAddCmd cmd);

    /**
     * 更新质量标准（仅DRAFT状态可更新）
     *
     * @param cmd 更新命令（cmd.id必填）
     */
    void updateStandard(QcQualityStandardAddCmd cmd);

    /**
     * 删除质量标准（草稿与已发布均可删除）
     *
     * @param id 标准ID
     */
    void deleteStandard(String id);

    /**
     * 发布质量标准
     *
     * @param id 标准ID
     * @return 包含 needingExpiryStandards 提示的Map
     */
    Map<String, Object> publishStandard(String id);

    /**
     * 分页查询质量标准
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<QcQualityStandardVO> page(QcQualityStandardPageQuery query);

    /**
     * 查询质量标准详情（含指标配置）
     *
     * @param id 标准ID
     * @return 详情VO
     */
    QcQualityStandardDetailVO getById(String id);

    /**
     * 查询指标项目列表（用于标准配置时的指标选择）
     *
     * @param keyword  关键词（指标名称/代码，可为null）
     * @param category 指标类别（可为null）
     * @return 指标列表
     */
    List<QcIndicatorItemVO> listIndicators(String keyword, String category);

    /**
     * 查询候选标准集，返回选中标准、被优先级抑制标准和冲突预留信息。
     *
     * @param query 候选标准匹配条件
     * @return 候选标准匹配结果
     */
    StandardCandidateSetVO findCandidateStandards(StandardCandidateQuery query);
}
