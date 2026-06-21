package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(value = "StandardConflictDraftVO", description = "候选标准冲突草稿")
public class StandardConflictDraftVO {

    @ApiModelProperty(value = "冲突类型")
    private String conflictType;

    @ApiModelProperty(value = "冲突级别 PRIORITY_RESOLVABLE/BLOCKING")
    private String conflictLevel;

    @ApiModelProperty(value = "状态 PENDING/RESOLVED")
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

    @ApiModelProperty(value = "涉及标准ID")
    private List<String> involvedStandardIds;

    @ApiModelProperty(value = "冲突明细JSON")
    private String conflictDetail;

    @ApiModelProperty(value = "优先级选择依据")
    private String selectedPriority;

    @ApiModelProperty(value = "可读提示")
    private String message;
}
