package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.StandardGap;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.StandardGapMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/standard-gaps")
@Api(tags = "标准覆盖缺口管理（FR-015）")
public class StandardGapController {

    @Resource
    private StandardGapMapper standardGapMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询标准覆盖缺口")
    public ApiResult<IPage<Map<String, Object>>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam(value = "品种") @RequestParam(required = false) String variety,
            @ApiParam(value = "牌号") @RequestParam(required = false) String grade,
            @ApiParam(value = "是否已解决: 0未解决 1已解决") @RequestParam(required = false) Integer isResolved) {

        LambdaQueryWrapper<StandardGap> wrapper = new LambdaQueryWrapper<StandardGap>()
                .like(StringUtils.hasText(variety), StandardGap::getVariety, variety)
                .like(StringUtils.hasText(grade), StandardGap::getGrade, grade)
                .eq(isResolved != null, StandardGap::getIsResolved, isResolved)
                .orderByDesc(StandardGap::getFirstFoundTime);

        IPage<StandardGap> gapPage = standardGapMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量查询指标名称
        List<String> indicatorIds = gapPage.getRecords().stream()
                .map(StandardGap::getIndicatorId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> indicatorNameMap = new HashMap<>();
        if (!indicatorIds.isEmpty()) {
            List<QcIndicatorItem> items = indicatorItemMapper.selectList(
                    new LambdaQueryWrapper<QcIndicatorItem>().in(QcIndicatorItem::getId, indicatorIds));
            items.forEach(item -> indicatorNameMap.put(item.getId(), item.getIndicatorName()));
        }

        // 组装 VO
        IPage<Map<String, Object>> resultPage = gapPage.convert(gap -> {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", gap.getId());
            vo.put("variety", gap.getVariety());
            vo.put("grade", gap.getGrade());
            vo.put("indicatorId", gap.getIndicatorId());
            vo.put("indicatorName", indicatorNameMap.getOrDefault(gap.getIndicatorId(), gap.getIndicatorId()));
            vo.put("firstFoundTime", gap.getFirstFoundTime());
            vo.put("relatedRecordId", gap.getRelatedRecordId());
            vo.put("isResolved", gap.getIsResolved());
            return vo;
        });

        return ApiResult.success(resultPage);
    }

    @PutMapping("/{id}/resolve")
    @ApiOperation(value = "标记缺口已解决")
    public ApiResult<Void> resolve(@ApiParam(value = "缺口ID", required = true) @PathVariable String id) {
        StandardGap gap = standardGapMapper.selectById(id);
        if (gap == null) {
            return ApiResult.failure("缺口记录不存在，id=" + id);
        }
        gap.setIsResolved(1);
        standardGapMapper.updateById(gap);
        return ApiResult.success();
    }
}
