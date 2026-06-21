package com.jhict.quality.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AlternativeStockVO {

    private String id;
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
