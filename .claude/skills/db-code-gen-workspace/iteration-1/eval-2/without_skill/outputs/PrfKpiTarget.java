package com.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * KPI目标设置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("PRF_KPI_TARGET")
public class PrfKpiTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * KPI编码
     */
    @TableField("KPI_CODE")
    private String kpiCode;

    /**
     * KPI名称
     */
    @TableField("KPI_NAME")
    private String kpiName;

    /**
     * 目标值
     */
    @TableField("TARGET_VALUE")
    private BigDecimal targetValue;

    /**
     * 单位
     */
    @TableField("UNIT")
    private String unit;

    /**
     * 年份
     */
    @TableField("YEAR_NO")
    private String yearNo;

    /**
     * 月份
     */
    @TableField("MONTH_NO")
    private String monthNo;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    @TableField("IS_ENABLE")
    private Integer isEnable;

    /**
     * 公司ID
     */
    @TableField("COMPANY_ID")
    private String companyId;

    /**
     * 创建人
     */
    @TableField("CREATE_USER_NO")
    private String createUserNo;

    /**
     * 创建时间
     */
    @TableField("CREATE_DATE_TIME")
    private String createDateTime;

    /**
     * 更新人
     */
    @TableField("UPDATE_USER_NO")
    private String updateUserNo;

    /**
     * 更新时间
     */
    @TableField("UPDATE_DATE_TIME")
    private String updateDateTime;
}
