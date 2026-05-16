package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StandardGapPageQuery", description = "标准覆盖缺口分页查询条件")
public class StandardGapPageQuery {

    @ApiModelProperty(value = "品种（模糊）")
    private String variety;

    @ApiModelProperty(value = "牌号（模糊）")
    private String grade;

    @ApiModelProperty(value = "是否已解决：0未解决 1已解决")
    private Integer isResolved;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
