package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard_document")
@ApiModel(value = "QcStandardDocument", description = "标准/协议/案例源文档")
public class QcStandardDocument extends CoreEntity {

    @ApiModelProperty(value = "结构化标准ID")
    private String standardId;

    @ApiModelProperty(value = "文档编号")
    private String documentCode;

    @ApiModelProperty(value = "文档名称")
    private String documentName;

    @ApiModelProperty(value = "文档类型 STANDARD/AGREEMENT/CASE/COMPLAINT")
    private String documentType;

    @ApiModelProperty(value = "标准类型 NATIONAL/ENTERPRISE/CUSTOMER")
    private String standardType;

    @ApiModelProperty(value = "标准编号快照")
    private String standardCode;

    @ApiModelProperty(value = "标准名称快照")
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

    @ApiModelProperty(value = "客户用途或适用场景")
    private String usageScope;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "旧版文件名字段，兼容历史表结构")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty(value = "旧版文件路径字段，兼容历史表结构")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty(value = "旧版文件类型字段，兼容历史表结构")
    @TableField("file_type")
    private String fileType;

    @ApiModelProperty(value = "旧版文件大小字段，兼容历史表结构")
    @TableField("file_size")
    private Long fileSize;

    @ApiModelProperty(value = "原始文件名")
    private String sourceFileName;

    @ApiModelProperty(value = "原始文件路径或URL")
    private String sourceFilePath;

    @ApiModelProperty(value = "原始文件哈希")
    private String sourceFileHash;

    @ApiModelProperty(value = "解析状态")
    private String parseStatus;

    @ApiModelProperty(value = "索引状态")
    private String indexStatus;

    @ApiModelProperty(value = "解析或索引错误")
    private String parseErrorMessage;

    @ApiModelProperty(value = "索引时间")
    private LocalDateTime indexedAt;

    @ApiModelProperty(value = "状态 ACTIVE/INACTIVE")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
