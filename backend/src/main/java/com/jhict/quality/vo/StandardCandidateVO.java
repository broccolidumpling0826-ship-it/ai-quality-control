package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel(value = "StandardCandidateVO", description = "候选标准")
public class StandardCandidateVO {

    @ApiModelProperty(value = "标准ID")
    private String id;

    @ApiModelProperty(value = "标准类型 CUSTOMER/ENTERPRISE/NATIONAL")
    private String standardType;

    @ApiModelProperty(value = "标准编号")
    private String standardCode;

    @ApiModelProperty(value = "标准名称")
    private String standardName;

    @ApiModelProperty(value = "版本号")
    private String versionNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "规格范围")
    private String specRange;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "优先级，1客户协议，2企标，3国标")
    private Integer priority;

    @ApiModelProperty(value = "是否被选中")
    private Boolean selected;

    @ApiModelProperty(value = "是否因更高优先级标准被抑制")
    private Boolean suppressed;

    @ApiModelProperty(value = "是否存在阻断冲突")
    private Boolean conflict;

    @ApiModelProperty(value = "匹配或抑制原因")
    private String reason;
}
