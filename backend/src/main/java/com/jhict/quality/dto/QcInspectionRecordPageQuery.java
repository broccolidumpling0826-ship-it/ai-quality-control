package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcInspectionRecordPageQuery", description = "检验记录分页查询条件")
public class QcInspectionRecordPageQuery {

    @ApiModelProperty(value = "卷号（模糊匹配）")
    private String coilNo;

    @ApiModelProperty(value = "炉号（模糊匹配）")
    private String heatNo;

    @ApiModelProperty(value = "记录状态（NORMAL/VOID）")
    private String status;

    @ApiModelProperty(value = "样品类型")
    private String sampleType;

    @ApiModelProperty(value = "检验时间起始（yyyy-MM-dd HH:mm:ss）")
    private String testTimeStart;

    @ApiModelProperty(value = "检验时间结束（yyyy-MM-dd HH:mm:ss）")
    private String testTimeEnd;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
