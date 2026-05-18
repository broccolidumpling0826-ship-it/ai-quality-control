package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "InspectionRecordCandidateVO", description = "复检关联候选检验记录")
public class InspectionRecordCandidateVO {

    @ApiModelProperty(value = "检验记录ID")
    private String id;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String productGrade;

    @ApiModelProperty(value = "检验时间")
    private LocalDateTime testTime;

    @ApiModelProperty(value = "样品类型")
    private String sampleType;
}
