package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcJudgmentPageQuery", description = "判定结论分页查询条件")
public class QcJudgmentPageQuery {

    @ApiModelProperty(value = "卷号（模糊，关联检验记录）")
    private String coilNo;

    @ApiModelProperty(value = "批次号（模糊，关联检验记录）")
    private String batchNo;

    @ApiModelProperty(value = "判定结论类型（QUALIFIED/UNQUALIFIED/NEED_REINSPECTION/CAN_CONCESSION/STANDARD_CONFLICT）")
    private String judgmentType;

    @ApiModelProperty(value = "是否最终结论：1是 0否")
    private Integer isFinal;

    @ApiModelProperty(value = "判定时间起始（yyyy-MM-dd HH:mm:ss）")
    private String timeStart;

    @ApiModelProperty(value = "判定时间结束（yyyy-MM-dd HH:mm:ss）")
    private String timeEnd;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;

    /** 兼容前端 startTime/endTime（仅日期） */
    public void setStartTime(String startTime) {
        if (startTime != null && !startTime.isEmpty()) {
            this.timeStart = startTime.length() <= 10 ? startTime + " 00:00:00" : startTime;
        }
    }

    public void setEndTime(String endTime) {
        if (endTime != null && !endTime.isEmpty()) {
            this.timeEnd = endTime.length() <= 10 ? endTime + " 23:59:59" : endTime;
        }
    }
}
