package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConcessionConfirmCmd", description = "客户确认让步命令")
public class ConcessionConfirmCmd {

    @ApiModelProperty(value = "确认备注（可选）")
    private String confirmNote;
}
