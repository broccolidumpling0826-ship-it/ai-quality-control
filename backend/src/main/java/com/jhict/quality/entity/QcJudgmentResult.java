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
@TableName("qc_judgment_result")
@ApiModel(value = "QcJudgmentResult", description = "判定结论")
public class QcJudgmentResult extends CoreEntity {

    @ApiModelProperty(value = "关联检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "判定结论（JudgmentType枚举）")
    private String judgmentType;

    @ApiModelProperty(value = "判定时间")
    private LocalDateTime judgmentTime;

    @ApiModelProperty(value = "是否当前最终结论：1是 0否（改判后旧结论置0）")
    private Integer isFinal;

    @ApiModelProperty(value = "命中标准ID列表（JSON数组）")
    private String matchedStandardIds;

    @ApiModelProperty(value = "备注")
    private String remark;
}
