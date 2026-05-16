package com.jhict.oa.api.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 请假记录 查询参数
 */
@Getter
@Setter
public class OaLeaveRecordQuery extends PageQuery {

    @ApiModelProperty(value = "员工工号")
    private String employeeNo;

    @ApiModelProperty(value = "部门编码")
    private String deptCode;

    @ApiModelProperty(value = "假期类型（1请假2调休3年假）")
    private String leaveType;

    @ApiModelProperty(value = "是否有效")
    private Boolean valid;

    // 按需添加其他查询字段
}
