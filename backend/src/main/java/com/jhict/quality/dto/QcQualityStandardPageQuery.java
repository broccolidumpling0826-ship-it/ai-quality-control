package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcQualityStandardPageQuery", description = "质量标准分页查询条件")
public class QcQualityStandardPageQuery {

    @ApiModelProperty(value = "标准类型（NATIONAL/ENTERPRISE/CUSTOMER）")
    private String standardType;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "状态（DRAFT/PUBLISHED/DEPRECATED）")
    private String status;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;

    /** 兼容前端 productVariety → variety */
    public void setProductVariety(String productVariety) {
        this.variety = productVariety;
    }

    /** 兼容前端 productGrade → grade */
    public void setProductGrade(String productGrade) {
        this.grade = productGrade;
    }
}
