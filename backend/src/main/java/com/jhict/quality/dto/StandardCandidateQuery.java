package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(value = "StandardCandidateQuery", description = "候选标准匹配查询")
public class StandardCandidateQuery {

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @NotBlank(message = "品种不能为空")
    @ApiModelProperty(value = "品种", required = true)
    private String variety;

    @NotBlank(message = "牌号不能为空")
    @ApiModelProperty(value = "牌号", required = true)
    private String grade;

    @ApiModelProperty(value = "产品规格")
    private String productSpec;

    @NotNull(message = "检验日期不能为空")
    @ApiModelProperty(value = "检验日期", required = true)
    private LocalDate testDate;

    @ApiModelProperty(value = "本次检验涉及的指标ID列表")
    private List<String> indicatorIds;

    /** 兼容前端 productVariety -> variety */
    public void setProductVariety(String productVariety) {
        this.variety = productVariety;
    }

    /** 兼容前端 productGrade -> grade */
    public void setProductGrade(String productGrade) {
        this.grade = productGrade;
    }
}
