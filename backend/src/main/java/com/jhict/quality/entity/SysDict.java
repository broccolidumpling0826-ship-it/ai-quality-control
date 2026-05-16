package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dict")
@ApiModel(value = "SysDict", description = "数据字典分类")
public class SysDict extends CoreEntity {

    @ApiModelProperty(value = "字典编码")
    private String dictCode;

    @ApiModelProperty(value = "字典名称")
    private String dictName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "是否系统内置：1是 0否")
    private Integer isSystem;

    @ApiModelProperty(value = "排序号")
    private Integer sortNo;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status;
}
