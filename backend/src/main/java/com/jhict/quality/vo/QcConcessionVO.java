package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel(value = "QcConcessionVO", description = "让步接收视图对象")
public class QcConcessionVO {

    @ApiModelProperty(value = "让步接收ID")
    private String id;

    @ApiModelProperty(value = "判定结论ID")
    private String judgmentId;

    @ApiModelProperty(value = "让步范围")
    private String concessionScope;

    @ApiModelProperty(value = "风险描述")
    private String riskDescription;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "到期日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "客户确认状态")
    private String confirmStatus;

    @ApiModelProperty(value = "客户确认附件URL")
    private String confirmAttachmentUrl;

    @ApiModelProperty(value = "客户确认备注")
    private String confirmNote;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "剩余天数（到期日距今天数，已过期为负数）")
    private int remainingDays;
}
