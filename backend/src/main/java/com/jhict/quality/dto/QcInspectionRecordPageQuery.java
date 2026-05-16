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

    /** 兼容前端 startTime/endTime（仅日期）→ testTimeStart/testTimeEnd */
    public void setStartTime(String startTime) {
        if (startTime != null && !startTime.isEmpty()) {
            this.testTimeStart = startTime.length() <= 10 ? startTime + " 00:00:00" : startTime;
        }
    }

    public void setEndTime(String endTime) {
        if (endTime != null && !endTime.isEmpty()) {
            this.testTimeEnd = endTime.length() <= 10 ? endTime + " 23:59:59" : endTime;
        }
    }
}
