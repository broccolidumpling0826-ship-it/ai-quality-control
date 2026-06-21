package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PreparedClauseIndexCmd", description = "已准备标准条款索引命令")
public class PreparedClauseIndexCmd {

    @ApiModelProperty(value = "源文档ID，为空时按其他条件索引")
    private String documentId;

    @ApiModelProperty(value = "演示/评测分组，如 P0_STANDARD/P0_CUSTOMER/P0_CONFLICT/P0_CASE")
    private String relevanceGroup;

    @ApiModelProperty(value = "是否只索引未成功条款，默认true")
    private Boolean onlyPending = Boolean.TRUE;

    @ApiModelProperty(value = "目标索引名，为空使用默认配置")
    private String indexName;
}
