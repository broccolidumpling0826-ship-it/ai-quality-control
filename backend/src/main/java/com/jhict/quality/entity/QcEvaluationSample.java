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
@TableName("qc_evaluation_sample")
@ApiModel(value = "QcEvaluationSample", description = "AI 评测样例")
public class QcEvaluationSample extends CoreEntity {

    @ApiModelProperty(value = "样例编码")
    private String sampleCode;

    @ApiModelProperty(value = "分类")
    private String category;

    @ApiModelProperty(value = "输入 JSON")
    private String inputPayload;

    @ApiModelProperty(value = "期望结果 JSON")
    private String expectedOutcome;

    @ApiModelProperty(value = "权重")
    private BigDecimal weight;
}
