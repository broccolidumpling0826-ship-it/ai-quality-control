package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ReinspectionCandidateQuery", description = "复检关联候选检验记录查询")
public class ReinspectionCandidateQuery {

    @ApiModelProperty(value = "炉号（模糊）")
    private String heatNo;

    @ApiModelProperty(value = "卷号（模糊）")
    private String coilNo;

    @ApiModelProperty(value = "批次号（模糊）")
    private String batchNo;

    @ApiModelProperty(value = "客户ID（精确）")
    private String customerId;

    @ApiModelProperty(value = "需排除的原检验记录ID（一般由后端自动解析，前端可透传）")
    private String excludeRecordId;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
