package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel(value = "QcQualityStandardVO", description = "质量标准列表视图对象")
public class QcQualityStandardVO {

    @ApiModelProperty(value = "标准ID")
    private String id;

    @ApiModelProperty(value = "标准类型")
    private String standardType;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "品种（前端别名 productVariety）")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "牌号（前端别名 productGrade）")
    private String productGrade;

    @ApiModelProperty(value = "规格范围")
    private String specRange;

    @ApiModelProperty(value = "版本号（前端别名 version）")
    private String version;

    @ApiModelProperty(value = "版本号")
    private String versionNo;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "关联指标数量")
    private Integer indicatorCount;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "描述（前端别名 description）")
    private String description;
}
