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
@TableName("standard_conflict")
@ApiModel(value = "StandardConflict", description = "标准冲突记录")
public class StandardConflict extends CoreEntity {

    @ApiModelProperty(value = "冲突编号")
    private String conflictNo;

    @ApiModelProperty(value = "关联判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "关联检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "冲突类型")
    private String conflictType;

    @ApiModelProperty(value = "冲突级别 PRIORITY_RESOLVABLE/BLOCKING")
    private String conflictLevel;

    @ApiModelProperty(value = "状态 PENDING/RESOLVED/VOID")
    private String status;

    @ApiModelProperty(value = "指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "规格")
    private String productSpec;

    @ApiModelProperty(value = "检验日期")
    private LocalDate inspectionDate;

    @ApiModelProperty(value = "优先级已选择标准ID")
    private String selectedStandardId;

    @ApiModelProperty(value = "涉及标准ID列表JSON")
    private String involvedStandardIds;

    @ApiModelProperty(value = "冲突明细JSON")
    private String conflictDetail;

    @ApiModelProperty(value = "优先级选择依据")
    private String selectedPriority;

    @ApiModelProperty(value = "裁决控制标准ID")
    private String decisionStandardId;

    @ApiModelProperty(value = "裁决理由")
    private String decisionReason;

    @ApiModelProperty(value = "裁决人工号")
    private String decisionBy;

    @ApiModelProperty(value = "裁决时间")
    private LocalDateTime decisionTime;

    @ApiModelProperty(value = "裁决后新判定ID")
    private String rejudgeJudgmentId;
}
