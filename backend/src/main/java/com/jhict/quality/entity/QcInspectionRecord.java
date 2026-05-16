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
@TableName("qc_inspection_record")
@ApiModel(value = "QcInspectionRecord", description = "检验记录")
public class QcInspectionRecord extends CoreEntity {

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号（业务聚合键）")
    private String batchNo;

    @ApiModelProperty(value = "样品类型")
    private String sampleType;

    @ApiModelProperty(value = "检验时间")
    private LocalDateTime testTime;

    @ApiModelProperty(value = "检验人工号")
    private String testerNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String productGrade;

    @ApiModelProperty(value = "规格")
    private String productSpec;

    @ApiModelProperty(value = "记录状态（NORMAL/VOID）")
    private String status;

    @ApiModelProperty(value = "作废原因")
    private String voidReason;

    @ApiModelProperty(value = "作废操作人工号")
    private String voidBy;

    @ApiModelProperty(value = "作废时间")
    private LocalDateTime voidTime;
}
