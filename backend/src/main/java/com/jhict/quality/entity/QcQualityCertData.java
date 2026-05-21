package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_quality_cert_data")
@ApiModel(value = "QcQualityCertData", description = "质保书数据")
public class QcQualityCertData extends CoreEntity {

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "快照数据（JSON，Text类型）")
    @TableField("snapshot_data")
    private String snapshotData;

    @ApiModelProperty(value = "生成时间")
    private LocalDateTime generateTime;

    @ApiModelProperty(value = "生成人工号")
    private String generatedBy;

    /** 分页列表查询时由 SQL 计算，不落库 */
    @TableField(value = "list_status", exist = false)
    @ApiModelProperty(value = "列表状态 SUCCESS/FAILED")
    private String listStatus;
}
