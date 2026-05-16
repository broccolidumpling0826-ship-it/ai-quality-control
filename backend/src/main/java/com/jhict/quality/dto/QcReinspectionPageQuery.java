package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcReinspectionPageQuery", description = "复检记录分页查询条件")
public class QcReinspectionPageQuery {

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "状态（PENDING/COMPLETED）")
    private String status;

    @ApiModelProperty(value = "责任人工号")
    private String responsibleNo;
}
