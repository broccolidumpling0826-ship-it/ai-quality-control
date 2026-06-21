package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "EvaluationRunCmd", description = "评测运行请求")
public class EvaluationRunCmd {

    @ApiModelProperty(value = "Prompt 版本")
    private String promptVersion;

    @ApiModelProperty(value = "评测分类过滤")
    private List<String> categories;
}
