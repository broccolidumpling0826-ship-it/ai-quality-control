package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "StandardConflictVO", description = "标准冲突视图")
public class StandardConflictVO {

    @ApiModelProperty(value = "冲突ID")
    private String id;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "标准A ID")
    private String standardIdA;

    @ApiModelProperty(value = "标准A 名称")
    private String standardNameA;

    @ApiModelProperty(value = "标准B ID")
    private String standardIdB;

    @ApiModelProperty(value = "标准B 名称")
    private String standardNameB;

    @ApiModelProperty(value = "A上限")
    private BigDecimal limitAUpper;

    @ApiModelProperty(value = "A下限")
    private BigDecimal limitALower;

    @ApiModelProperty(value = "B上限")
    private BigDecimal limitBUpper;

    @ApiModelProperty(value = "B下限")
    private BigDecimal limitBLower;

    @ApiModelProperty(value = "冲突状态")
    private String conflictStatus;

    @ApiModelProperty(value = "裁定说明")
    private String resolutionNote;

    @ApiModelProperty(value = "引用列表")
    private List<CitationVO> citations;
}
