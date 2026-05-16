package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcAuditLogPageQuery", description = "审计日志分页查询条件")
public class QcAuditLogPageQuery {

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "操作人工号")
    private String operatorNo;

    @ApiModelProperty(value = "开始时间（yyyy-MM-dd HH:mm:ss）")
    private String timeStart;

    @ApiModelProperty(value = "结束时间（yyyy-MM-dd HH:mm:ss）")
    private String timeEnd;

    @ApiModelProperty(value = "开始时间（前端字段名，与 timeStart 二选一）")
    private String startTime;

    @ApiModelProperty(value = "结束时间（前端字段名，与 timeEnd 二选一）")
    private String endTime;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = 10;
}
