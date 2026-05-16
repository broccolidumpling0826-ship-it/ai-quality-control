package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DictItemVO", description = "字典项视图对象")
public class DictItemVO {

    @ApiModelProperty(value = "字典项值（对应 itemValue）")
    private String value;

    @ApiModelProperty(value = "字典项标签（对应 itemLabel）")
    private String label;

    @ApiModelProperty(value = "颜色标签")
    private String colorTag;

    @ApiModelProperty(value = "排序号")
    private Integer sortNo;
}
