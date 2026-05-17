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

    @ApiModelProperty(value = "确认附件上传人工号")
    private String confirmUploaderNo;

    @ApiModelProperty(value = "确认附件上传人姓名")
    private String confirmUploadByName;

    @ApiModelProperty(value = "确认附件上传时间")
    private String confirmUploadTime;

    @ApiModelProperty(value = "确认附件原始文件名")
    private String confirmFileName;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "让步总状态（字典 CONCESSION_STATUS，供详情展示）")
    private String concessionStatus;

    @ApiModelProperty(value = "剩余天数（到期日距今天数，已过期为负数）")
    private int remainingDays;

    // ---------- 前端展示别名（与 effectiveDate/expiryDate 等同步填充） ----------

    @ApiModelProperty(value = "生效日期（前端别名 validFrom）")
    private LocalDate validFrom;

    @ApiModelProperty(value = "到期日期（前端别名 validTo）")
    private LocalDate validTo;

    @ApiModelProperty(value = "客户确认附件（前端别名 confirmFileUrl）")
    private String confirmFileUrl;

    @ApiModelProperty(value = "卷号（关联检验记录）")
    private String coilNo;

    @ApiModelProperty(value = "批次号（关联检验记录）")
    private String batchNo;

    @ApiModelProperty(value = "申请人工号")
    private String applyBy;

    @ApiModelProperty(value = "申请人姓名")
    private String applyByName;

    @ApiModelProperty(value = "申请时间")
    private String applyTime;

    @ApiModelProperty(value = "让步原因（前端 detail.reason，映射 riskDescription）")
    private String reason;
}
