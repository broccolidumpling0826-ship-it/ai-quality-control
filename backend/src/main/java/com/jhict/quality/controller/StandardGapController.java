package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.StandardGapPageQuery;
import com.jhict.quality.service.api.StandardGapService;
import com.jhict.quality.vo.StandardGapVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/standard-gaps")
@Api(tags = "标准覆盖缺口管理（FR-015）")
public class StandardGapController {

    @Resource
    private StandardGapService standardGapService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询标准覆盖缺口")
    public ApiResult<IPage<StandardGapVO>> page(@ModelAttribute StandardGapPageQuery query) {
        return ApiResult.success(standardGapService.pageStandardGaps(query));
    }

    @PutMapping("/{id}/resolve")
    @ApiOperation(value = "标记缺口已解决")
    public ApiResult<Void> resolve(
            @ApiParam(value = "缺口ID", required = true) @PathVariable String id) {
        standardGapService.resolveStandardGap(id);
        return ApiResult.success();
    }
}
