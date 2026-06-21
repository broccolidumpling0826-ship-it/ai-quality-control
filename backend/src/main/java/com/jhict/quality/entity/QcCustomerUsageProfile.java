package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_customer_usage_profile")
public class QcCustomerUsageProfile extends CoreEntity {

    private String customerId;
    private String customerName;
    private String defaultUsage;
    private String riskCategory;
    private String variety;
    private String grade;
    private String status;
    private String remark;
}
