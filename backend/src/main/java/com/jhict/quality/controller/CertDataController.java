package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcQualityCertGenerateCmd;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.vo.QcQualityCertDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/cert-data")
@Api(tags = "质保书数据管理")
public class CertDataController {

    @Resource
    private CertDataService certDataService;

    @PostMapping("/generate")
    @ApiOperation(value = "生成质保书数据")
    public ApiResult<String> generate(@Validated @RequestBody QcQualityCertGenerateCmd cmd) {
        return ApiResult.success(certDataService.generate(cmd));
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询质保书数据")
    public ApiResult<IPage<QcQualityCertDataVO>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "卷号") @RequestParam(required = false) String coilNo,
            @ApiParam(value = "批次号") @RequestParam(required = false) String batchNo) {
        return ApiResult.success(certDataService.page(pageNum, pageSize, coilNo, batchNo));
    }
}
