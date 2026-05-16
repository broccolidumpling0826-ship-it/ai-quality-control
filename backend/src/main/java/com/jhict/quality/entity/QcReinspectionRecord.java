package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_reinspection_record")
@ApiModel(value = "QcReinspectionRecord", description = "复检记录")
public class QcReinspectionRecord extends CoreEntity {

    @ApiModelProperty(value = "原判定结论ID")
    private String originalJudgmentId;

    @ApiModelProperty(value = "复检原因（最多500字）")
    private String reinspectionReason;

    @ApiModelProperty(value = "关联新检验记录ID（完成后填写）")
    private String newRecordId;

    @ApiModelProperty(value = "责任人工号")
    private String responsibleNo;

    @ApiModelProperty(value = "状态（PENDING/COMPLETED）")
    private String status;
}
