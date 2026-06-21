package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "StandardCandidateSetVO", description = "候选标准匹配结果")
public class StandardCandidateSetVO {

    @ApiModelProperty(value = "最终选择的结构化标准")
    private StandardCandidateVO selectedStandard;

    @ApiModelProperty(value = "全部候选标准")
    private List<StandardCandidateVO> candidateStandards;

    @ApiModelProperty(value = "因优先级被抑制但仍适用的标准")
    private List<StandardCandidateVO> suppressedStandards;

    @ApiModelProperty(value = "冲突标准，4.1仅保留契约，后续冲突检测填充")
    private List<StandardCandidateVO> conflictStandards;

    @ApiModelProperty(value = "候选标准冲突明细")
    private List<StandardConflictDraftVO> conflicts;

    @ApiModelProperty(value = "匹配提示或冲突预警")
    private List<String> warnings;
}
