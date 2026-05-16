package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "StandardGapVO", description = "标准覆盖缺口列表项")
public class StandardGapVO {

    @ApiModelProperty(value = "缺口ID")
    private String id;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "缺口指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "缺口指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "首次发现时间")
    private LocalDateTime firstFoundTime;

    @ApiModelProperty(value = "关联检验记录ID")
    private String relatedRecordId;

    @ApiModelProperty(value = "是否已解决：1已解决 0未解决")
    private Integer isResolved;
}
