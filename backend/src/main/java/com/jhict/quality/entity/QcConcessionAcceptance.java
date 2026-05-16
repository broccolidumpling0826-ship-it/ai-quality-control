package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_concession_acceptance")
@ApiModel(value = "QcConcessionAcceptance", description = "让步接收申请")
public class QcConcessionAcceptance extends CoreEntity {

    @ApiModelProperty(value = "关联判定结论ID")
    private String judgmentId;

    @ApiModelProperty(value = "让步范围（最多1000字）")
    private String concessionScope;

    @ApiModelProperty(value = "风险描述（最多1000字）")
    private String riskDescription;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "到期日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "客户确认状态（PENDING/CONFIRMED/REJECTED）")
    private String confirmStatus;

    @ApiModelProperty(value = "客户确认附件URL")
    private String confirmAttachmentUrl;

    @ApiModelProperty(value = "客户确认备注（最多1000字）")
    private String confirmNote;

    @ApiModelProperty(value = "确认附件上传人工号")
    private String confirmUploaderNo;

    @ApiModelProperty(value = "确认附件上传时间")
    private LocalDateTime confirmUploadTime;

    @ApiModelProperty(value = "审批状态（PENDING/APPROVED/REJECTED/INVALID）")
    private String approvalStatus;

    @ApiModelProperty(value = "作废原因")
    private String voidReason;

    @ApiModelProperty(value = "最后催确认时间")
    private LocalDateTime lastReminderTime;
}
