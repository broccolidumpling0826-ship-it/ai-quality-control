package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcReinspectionListVO", description = "复检记录列表视图（含原判定与检验信息）")
public class QcReinspectionListVO {

    @ApiModelProperty(value = "复检记录ID")
    private String id;

    @ApiModelProperty(value = "原判定结论ID")
    private String originalJudgmentId;

    @ApiModelProperty(value = "复检原因")
    private String reinspectionReason;

    @ApiModelProperty(value = "责任人工号")
    private String responsibleNo;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "新检验记录ID")
    private String newRecordId;

    @ApiModelProperty(value = "发起时间")
    private String createDateTime;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "原检验记录ID（完成复检时需排除）")
    private String originalRecordId;

    @ApiModelProperty(value = "原判定结论类型")
    private String originalJudgmentType;
}
