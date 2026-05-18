package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcJudgmentListVO", description = "判定结论列表视图（含检验记录关联字段）")
public class QcJudgmentListVO {

    @ApiModelProperty(value = "判定结论ID")
    private String id;

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "判定结论类型")
    private String judgmentType;

    @ApiModelProperty(value = "判定时间")
    private String judgmentTime;

    @ApiModelProperty(value = "是否最终结论：1是 0否")
    private Integer isFinal;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "品种")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String productGrade;

    @ApiModelProperty(value = "样品类型")
    private String sampleType;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "检验人工号")
    private String testerNo;

    @ApiModelProperty(value = "检验人姓名")
    private String inspector;
}
