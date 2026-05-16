package com.example.query;

import lombok.Data;

/**
 * KPI目标设置 查询条件对象
 */
@Data
public class PrfKpiTargetQuery {

    /**
     * KPI编码（精确匹配）
     */
    private String kpiCode;

    /**
     * KPI名称（模糊匹配）
     */
    private String kpiName;

    /**
     * 年份
     */
    private String yearNo;

    /**
     * 月份
     */
    private String monthNo;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    private Integer isEnable;

    /**
     * 公司ID
     */
    private String companyId;

    /**
     * 当前页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10条）
     */
    private Integer pageSize = 10;
}
