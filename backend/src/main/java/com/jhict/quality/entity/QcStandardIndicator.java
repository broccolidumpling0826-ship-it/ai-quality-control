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
@TableName("qc_standard_indicator")
@ApiModel(value = "QcStandardIndicator", description = "标准指标")
public class QcStandardIndicator extends CoreEntity {

    @ApiModelProperty(value = "关联质量标准ID")
    private String standardId;

    @ApiModelProperty(value = "关联指标项目ID")
    private String indicatorId;

    @ApiModelProperty(value = "上限（NULL表示无上限）")
    private BigDecimal upperLimit;

    @ApiModelProperty(value = "下限（NULL表示无下限）")
    private BigDecimal lowerLimit;

    @ApiModelProperty(value = "是否必检：1必检 0非必检")
    private Integer isRequired;

    @ApiModelProperty(value = "让步上限（NULL表示不可让步）")
    private BigDecimal concessionUpper;

    @ApiModelProperty(value = "让步下限")
    private BigDecimal concessionLower;
}
