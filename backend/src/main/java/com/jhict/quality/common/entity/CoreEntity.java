package com.jhict.quality.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CoreEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    protected String id;

    @ApiModelProperty(value = "公司ID")
    @TableField(fill = FieldFill.INSERT)
    protected String companyId;

    @ApiModelProperty(value = "创建人工号")
    @TableField(fill = FieldFill.INSERT)
    protected String createUserNo;

    @ApiModelProperty(value = "更新人工号")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateUserNo;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    protected String createDateTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateDateTime;
}
