package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "CertQaAnswerVO", description = "质保书问答响应")
public class CertQaAnswerVO {

    @ApiModelProperty(value = "回答")
    private String answer;

    @ApiModelProperty(value = "是否拒答")
    private Boolean refused;

    @ApiModelProperty(value = "拒答原因")
    private String refusalReason;

    @ApiModelProperty(value = "是否非最终说明")
    private Boolean nonFinal;

    @ApiModelProperty(value = "是否有质保书快照")
    private Boolean certificateSnapshotFound;

    @ApiModelProperty(value = "操作引导文案（如让步已批准但尚未生成质保书快照）")
    private String guidanceMessage;

    @ApiModelProperty(value = "让步接收是否已审批通过")
    private Boolean concessionApproved;

    @ApiModelProperty(value = "是否命中缓存")
    private Boolean cacheHit;

    @ApiModelProperty(value = "置信度标签")
    private String confidenceLabel;

    @ApiModelProperty(value = "降级来源")
    private String degradationSource;

    @ApiModelProperty(value = "来源引用")
    private List<AiSourceReferenceVO> citations;

    @ApiModelProperty(value = "相关指标")
    private List<QcJudgmentResultVO.EvidenceVO> indicatorBasis;
}
