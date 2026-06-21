package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard_conflict")
@ApiModel(value = "QcStandardConflict", description = "标准冲突记录")
public class QcStandardConflict extends CoreEntity {

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "冲突指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "标准A")
    private String standardIdA;

    @ApiModelProperty(value = "标准B")
    private String standardIdB;

    @ApiModelProperty(value = "A上限")
    private BigDecimal limitAUpper;

    @ApiModelProperty(value = "A下限")
    private BigDecimal limitALower;

    @ApiModelProperty(value = "B上限")
    private BigDecimal limitBUpper;

    @ApiModelProperty(value = "B下限")
    private BigDecimal limitBLower;

    @ApiModelProperty(value = "冲突状态")
    private String conflictStatus;

    @ApiModelProperty(value = "裁定说明")
    private String resolutionNote;

    @ApiModelProperty(value = "裁定人")
    private String resolvedBy;

    @ApiModelProperty(value = "裁定时间")
    private String resolvedTime;

    @ApiModelProperty(value = "演示样例标记")
    private Integer demoFlag;
}
