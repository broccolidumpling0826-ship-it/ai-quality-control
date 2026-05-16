package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_indicator_item")
@ApiModel(value = "QcIndicatorItem", description = "指标项目")
public class QcIndicatorItem extends CoreEntity {

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "指标代码")
    private String indicatorCode;

    @ApiModelProperty(value = "指标类别")
    private String indicatorCategory;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "检测方法")
    private String testMethod;

    @ApiModelProperty(value = "备注说明")
    private String description;

    @ApiModelProperty(value = "状态 ACTIVE/INACTIVE")
    private String status;

    @ApiModelProperty(value = "软删除标志：0未删除 1已删除")
    @TableLogic
    private Integer isDeleted;
}
