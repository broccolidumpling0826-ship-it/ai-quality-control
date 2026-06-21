package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard_clause")
@ApiModel(value = "QcStandardClause", description = "标准/协议/案例条款")
public class QcStandardClause extends CoreEntity {

    @ApiModelProperty(value = "源文档ID")
    private String documentId;

    @ApiModelProperty(value = "结构化标准ID")
    private String standardId;

    @ApiModelProperty(value = "稳定条款键")
    private String clauseKey;

    @ApiModelProperty(value = "条款号")
    private String clauseNo;

    @ApiModelProperty(value = "页码")
    private Integer pageNo;

    @ApiModelProperty(value = "来源段落原文")
    private String paragraphText;

    @ApiModelProperty(value = "来源类型 NATIONAL/ENTERPRISE/CUSTOMER/CASE/COMPLAINT")
    private String sourceType;

    @ApiModelProperty(value = "标准类型快照")
    private String standardType;

    @ApiModelProperty(value = "标准编号快照")
    private String standardCode;

    @ApiModelProperty(value = "标准名称快照")
    private String standardName;

    @ApiModelProperty(value = "版本号快照")
    private String versionNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "适用品种")
    private String variety;

    @ApiModelProperty(value = "适用牌号")
    private String grade;

    @ApiModelProperty(value = "适用规格范围")
    private String specRange;

    @ApiModelProperty(value = "客户用途或适用场景")
    private String usageScope;

    @ApiModelProperty(value = "关联指标ID")
    private String indicatorId;

    @ApiModelProperty(value = "指标代码")
    private String indicatorCode;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "检索关键词")
    private String retrievalKeywords;

    @ApiModelProperty(value = "ES文档键")
    private String esDocumentKey;

    @ApiModelProperty(value = "向量状态")
    private String embeddingStatus;

    @ApiModelProperty(value = "演示/评测分组")
    private String relevanceGroup;

    @ApiModelProperty(value = "状态 ACTIVE/INACTIVE")
    private String status;
}
