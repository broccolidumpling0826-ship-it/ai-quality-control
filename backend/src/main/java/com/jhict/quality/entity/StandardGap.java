package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("standard_gap")
@ApiModel(value = "StandardGap", description = "标准缺口记录")
public class StandardGap extends CoreEntity {

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "缺口指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "首次发现时间")
    private LocalDateTime firstFoundTime;

    @ApiModelProperty(value = "关联检验记录ID")
    private String relatedRecordId;

    @ApiModelProperty(value = "是否已解决：1已解决 0未解决")
    private Integer isResolved;
}
