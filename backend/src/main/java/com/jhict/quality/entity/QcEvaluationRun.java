package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_evaluation_run")
@ApiModel(value = "QcEvaluationRun", description = "AI 评测运行批次")
public class QcEvaluationRun extends CoreEntity {

    @ApiModelProperty(value = "运行编号")
    private String runNo;

    @ApiModelProperty(value = "Prompt 版本")
    private String promptVersion;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "总样例数")
    private Integer totalSamples;

    @ApiModelProperty(value = "通过样例数")
    private Integer passedSamples;

    @ApiModelProperty(value = "规则通过率")
    private BigDecimal rulePassRate;

    @ApiModelProperty(value = "AI 准确率")
    private BigDecimal aiAccuracyRate;

    @ApiModelProperty(value = "引用命中率")
    private BigDecimal citationHitRate;

    @ApiModelProperty(value = "平均耗时毫秒")
    private Integer avgLatencyMs;

    @ApiModelProperty(value = "需复核命中率")
    private BigDecimal manualReviewHitRate;

    @ApiModelProperty(value = "完整报告 JSON")
    private String reportJson;

    @ApiModelProperty(value = "完成时间")
    private String finishedTime;
}
