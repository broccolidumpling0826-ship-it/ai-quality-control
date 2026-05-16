package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcConcessionPageQuery", description = "让步接收分页查询条件")
public class QcConcessionPageQuery {

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "客户确认状态")
    private String confirmStatus;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "卷号（模糊）")
    private String coilNo;
}
