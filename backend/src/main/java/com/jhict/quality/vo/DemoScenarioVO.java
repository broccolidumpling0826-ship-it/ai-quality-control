package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DemoScenarioVO", description = "决赛演示快捷入口")
public class DemoScenarioVO {

    @ApiModelProperty(value = "演示编码")
    private String demoCode;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "判定类型")
    private String judgmentType;

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "路由路径")
    private String routePath;
}
