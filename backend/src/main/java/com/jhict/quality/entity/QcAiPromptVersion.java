package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_prompt_version")
@ApiModel(value = "QcAiPromptVersion", description = "AI Prompt 版本注册")
public class QcAiPromptVersion extends CoreEntity {

    @ApiModelProperty(value = "Prompt 键")
    private String promptKey;

    @ApiModelProperty(value = "版本号")
    private String versionNo;

    @ApiModelProperty(value = "classpath 文件路径")
    private String filePath;

    @ApiModelProperty(value = "是否激活")
    private Integer isActive;

    @ApiModelProperty(value = "描述")
    private String description;
}
