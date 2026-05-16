package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_quality_standard")
@ApiModel(value = "QcQualityStandard", description = "质量标准")
public class QcQualityStandard extends CoreEntity {

    @ApiModelProperty(value = "标准类型（NATIONAL/ENTERPRISE/CUSTOMER）")
    private String standardType;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "规格范围")
    private String specRange;

    @ApiModelProperty(value = "版本号")
    private String versionNo;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "状态（DRAFT/PUBLISHED/DEPRECATED）")
    private String status;

    @ApiModelProperty(value = "客户ID（仅客户协议标准有值）")
    private String customerId;

    @ApiModelProperty(value = "备注")
    private String remark;
}
