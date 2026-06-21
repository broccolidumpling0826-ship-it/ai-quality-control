package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StandardConflictPageQuery", description = "标准冲突分页查询")
public class StandardConflictPageQuery {

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "冲突级别 PRIORITY_RESOLVABLE/BLOCKING")
    private String conflictLevel;

    @ApiModelProperty(value = "状态 PENDING/RESOLVED/VOID")
    private String status;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
