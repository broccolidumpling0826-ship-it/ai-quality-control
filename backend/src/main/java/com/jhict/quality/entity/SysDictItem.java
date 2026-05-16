package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dict_item")
@ApiModel(value = "SysDictItem", description = "数据字典项")
public class SysDictItem extends CoreEntity {

    @ApiModelProperty(value = "所属字典编码")
    private String dictCode;

    @ApiModelProperty(value = "字典项值")
    private String itemValue;

    @ApiModelProperty(value = "字典项标签（中文）")
    private String itemLabel;

    @ApiModelProperty(value = "字典项标签（英文）")
    private String itemLabelEn;

    @ApiModelProperty(value = "颜色标签（前端标签色）")
    private String colorTag;

    @ApiModelProperty(value = "排序号")
    private Integer sortNo;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status;

    @ApiModelProperty(value = "是否系统内置：1是 0否")
    private Integer isSystem;

    @ApiModelProperty(value = "备注")
    private String remark;
}
