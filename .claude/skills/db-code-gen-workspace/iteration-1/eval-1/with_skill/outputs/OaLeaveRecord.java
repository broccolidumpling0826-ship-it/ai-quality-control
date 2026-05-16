package com.jhict.oa.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.common.core.entity.CoreEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 请假记录
 */
@Getter
@Setter
@TableName("OA_LEAVE_RECORD")
public class OaLeaveRecord extends CoreEntity {

    @ApiModelProperty(value = "员工工号")
    private String employeeNo;

    @ApiModelProperty(value = "部门编码")
    private String deptCode;

    /**
     * 假期类型：1-请假，2-调休，3-年假
     * TODO: 如需枚举管理，请按枚举规范创建 LeaveStatusType 枚举类
     */
    @ApiModelProperty(value = "假期类型（1请假2调休3年假）")
    private String leaveType;

    @ApiModelProperty(value = "请假天数")
    private BigDecimal leaveDays;

    @ApiModelProperty(value = "是否有效")
    private Boolean valid;

    @ApiModelProperty(value = "备注")
    private String remark;
}
