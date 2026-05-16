package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "InspectionResultVO", description = "检验提交结果视图对象")
public class InspectionResultVO {

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "判定结论ID")
    private String judgmentId;

    @ApiModelProperty(value = "判定结论类型")
    private String judgmentType;

    @ApiModelProperty(value = "判定时间（yyyy-MM-dd HH:mm:ss）")
    private String judgmentTime;
}
