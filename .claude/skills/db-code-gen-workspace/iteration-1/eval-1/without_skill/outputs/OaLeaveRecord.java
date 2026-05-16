package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 请假记录实体类
 */
@Data
@TableName("OA_LEAVE_RECORD")
public class OaLeaveRecord {

    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 假期类型（1请假 2调休 3年假）
     */
    private String leaveType;

    /**
     * 请假天数
     */
    private BigDecimal leaveDays;

    /**
     * 是否有效（默认1）
     */
    private Integer isValid;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人工号
     */
    private String createUserNo;

    /**
     * 创建日期
     */
    private String createDateTime;

    /**
     * 修改人工号
     */
    private String updateUserNo;

    /**
     * 修改日期
     */
    private String updateDateTime;

    /**
     * 公司ID
     */
    private String companyId;
}
