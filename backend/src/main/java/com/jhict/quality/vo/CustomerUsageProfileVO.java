package com.jhict.quality.vo;

import lombok.Data;

@Data
public class CustomerUsageProfileVO {

    private String id;
    private String customerId;
    private String customerName;
    private String defaultUsage;
    private String riskCategory;
    private String variety;
    private String grade;
    private String remark;
}
