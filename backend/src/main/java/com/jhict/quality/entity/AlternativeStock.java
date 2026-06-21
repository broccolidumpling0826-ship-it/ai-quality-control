package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alternative_stock")
public class AlternativeStock extends CoreEntity {

    private String variety;
    private String grade;
    private String specRange;
    private String coilNo;
    private String batchNo;
    private BigDecimal availableWeight;
    private String location;
    private String status;
    private LocalDate earliestShipDate;
    private String remark;
}
