package com.example.query;

import lombok.Data;

/**
 * 请假记录查询条件
 */
@Data
public class OaLeaveRecordQuery {

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
     * 是否有效
     */
    private Integer isValid;

    /**
     * 公司ID
     */
    private String companyId;

    /**
     * 创建日期起始（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    private String createDateTimeStart;

    /**
     * 创建日期结束（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    private String createDateTimeEnd;

    /**
     * 当前页码，默认第1页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认10条
     */
    private Integer pageSize = 10;
}
