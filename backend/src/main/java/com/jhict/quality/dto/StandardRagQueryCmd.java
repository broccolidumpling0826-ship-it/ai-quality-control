package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel(value = "StandardRagQueryCmd", description = "标准RAG自然语言检索命令")
public class StandardRagQueryCmd {

    @NotBlank(message = "查询问题不能为空")
    @ApiModelProperty(value = "自然语言问题", required = true)
    private String query;

    @ApiModelProperty(value = "来源类型过滤 NATIONAL/ENTERPRISE/CUSTOMER/CASE/COMPLAINT")
    private List<String> sourceTypes;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "指标代码")
    private String indicatorCode;

    @ApiModelProperty(value = "适用日期 yyyy-MM-dd")
    private String effectiveDate;

    @ApiModelProperty(value = "返回条款数量，默认5")
    private Integer topK;
}
