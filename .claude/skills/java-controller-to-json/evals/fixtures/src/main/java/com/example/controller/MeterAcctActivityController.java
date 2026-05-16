package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.MeterAcctActivityDTO;
import com.example.dto.MeterAcctActivityQueryDTO;
import com.example.entity.MeterAcctActivity;
import com.example.service.MeterAcctActivityService;
import com.example.common.ApiResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meterAcctActivity")
@Api(value = "计量核算活动")
public class MeterAcctActivityController {

    private final MeterAcctActivityService service;

    @ApiOperation(value = "新增计量核算活动", notes = "新增计量核算活动")
    @ApiImplicitParams(value = {})
    @PostMapping("/save")
    public ApiResult<MeterAcctActivity> save(@RequestBody MeterAcctActivityDTO entityDto) {
        service.save(entityDto);
        return ApiResult.success("新增成功!", entityDto);
    }

    @ApiOperation(value = "修改计量核算活动", notes = "修改计量核算活动")
    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody MeterAcctActivityDTO entityDto) {
        service.update(entityDto);
        return ApiResult.success("修改成功!");
    }

    @ApiOperation(value = "删除计量核算活动", notes = "根据ID删除")
    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return ApiResult.success("删除成功!");
    }

    @ApiOperation(value = "查询计量核算活动列表（分页）", notes = "分页查询")
    @PostMapping("/list")
    public ApiResult<Page<MeterAcctActivity>> list(@RequestBody MeterAcctActivityQueryDTO query) {
        Page<MeterAcctActivity> page = service.listPage(query);
        return ApiResult.success(page);
    }

    @ApiOperation(value = "根据ID查询计量核算活动详情")
    @GetMapping("/detail/{id}")
    public ApiResult<MeterAcctActivity> detail(@PathVariable Long id) {
        MeterAcctActivity entity = service.getById(id);
        return ApiResult.success(entity);
    }
}
