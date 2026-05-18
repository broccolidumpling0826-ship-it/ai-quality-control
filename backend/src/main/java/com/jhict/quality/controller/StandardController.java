package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcQualityStandardAddCmd;
import com.jhict.quality.dto.QcQualityStandardPageQuery;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.QcQualityStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/standards")
@Api(tags = "质量标准管理")
public class StandardController {

    @Resource
    private StandardService standardService;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @PostMapping
    @ApiOperation(value = "新增质量标准")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<String> addStandard(@Validated @RequestBody QcQualityStandardAddCmd cmd) {
        String id = standardService.addStandard(cmd);
        return ApiResult.success("新增成功", id);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新质量标准（仅DRAFT状态可更新）")
    public ApiResult<Void> updateStandard(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id,
            @Validated @RequestBody QcQualityStandardAddCmd cmd) {
        cmd.setId(id);
        standardService.updateStandard(cmd);
        return ApiResult.success();
    }

    @PutMapping("/{id}/publish")
    @ApiOperation(value = "发布质量标准")
    public ApiResult<Map<String, Object>> publishStandard(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id) {
        Map<String, Object> result = standardService.publishStandard(id);
        return ApiResult.success(result);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除质量标准")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<Void> deleteStandard(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id) {
        standardService.deleteStandard(id);
        return ApiResult.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询质量标准")
    public ApiResult<IPage<QcQualityStandardVO>> page(@RequestBody QcQualityStandardPageQuery query) {
        return ApiResult.success(standardService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询质量标准详情（含指标配置）")
    public ApiResult<QcQualityStandardDetailVO> getById(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardService.getById(id));
    }

    @GetMapping("/spec-ranges")
    @ApiOperation(value = "查询有效规格范围列表（用于检验录入规格下拉选择，D-016）")
    public ApiResult<List<Map<String, String>>> getSpecRanges(
            @ApiParam(value = "品种", required = true) @RequestParam String variety,
            @ApiParam(value = "牌号", required = true) @RequestParam String grade,
            @ApiParam(value = "客户ID（匹配客户协议标准）") @RequestParam(required = false) String customerId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<QcQualityStandard> wrapper = new LambdaQueryWrapper<QcQualityStandard>()
                .eq(QcQualityStandard::getVariety, variety)
                .eq(QcQualityStandard::getGrade, grade)
                .le(QcQualityStandard::getEffectiveDate, today)
                .ge(QcQualityStandard::getExpiryDate, today)
                .eq(QcQualityStandard::getStatus, "PUBLISHED");
        if (StringUtils.hasText(customerId)) {
            wrapper.and(w -> w.eq(QcQualityStandard::getCustomerId, customerId)
                    .or().isNull(QcQualityStandard::getCustomerId));
        }
        List<QcQualityStandard> standards = qualityStandardMapper.selectList(wrapper);
        List<Map<String, String>> result = standards.stream()
                .filter(s -> StringUtils.hasText(s.getSpecRange()))
                .map(s -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("value", s.getSpecRange());
                    item.put("label", s.getSpecRange() + " (" + s.getVersionNo() + ")");
                    item.put("standardId", s.getId());
                    return item;
                })
                .filter(m -> !m.isEmpty())
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(m -> m.get("value")))),
                        ArrayList::new));
        return ApiResult.success(result);
    }

    @GetMapping("/indicators")
    @ApiOperation(value = "查询可用指标列表（用于标准配置时指标选择）")
    public ApiResult<List<QcIndicatorItemVO>> listIndicators(
            @ApiParam(value = "关键词（指标名称/代码）") @RequestParam(required = false) String keyword,
            @ApiParam(value = "指标类别") @RequestParam(required = false) String category) {
        return ApiResult.success(standardService.listIndicators(keyword, category));
    }
}
