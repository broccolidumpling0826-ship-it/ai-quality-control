package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("qc_audit_log")
@ApiModel(value = "QcAuditLog", description = "审计日志")
public class QcAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID（雪花）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "公司ID")
    private String companyId;

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "目标实体名称")
    private String targetEntity;

    @ApiModelProperty(value = "目标实体ID")
    private String targetId;

    @ApiModelProperty(value = "操作前数据（JSON Text）")
    @TableField("before_value")
    private String beforeValue;

    @ApiModelProperty(value = "操作后数据（JSON Text）")
    @TableField("after_value")
    private String afterValue;

    @ApiModelProperty(value = "操作人工号")
    private String operatorNo;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operateTime;

    @ApiModelProperty(value = "客户端IP")
    private String ipAddress;

    @ApiModelProperty(value = "备注")
    private String remark;
}
