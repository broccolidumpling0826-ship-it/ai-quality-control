package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.SysDictPageQuery;
import com.jhict.quality.entity.SysDict;
import com.jhict.quality.entity.SysDictItem;
import com.jhict.quality.mapper.SysDictItemMapper;
import com.jhict.quality.mapper.SysDictMapper;
import com.jhict.quality.service.api.SysDictService;
import com.jhict.quality.vo.DictItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/dict")
@Api(tags = "数据字典模块")
public class DictController {

    @Resource
    private SysDictService sysDictService;

    @Resource
    private SysDictMapper sysDictMapper;

    @Resource
    private SysDictItemMapper sysDictItemMapper;

    // -------------------------------------------------------------------------
    // Read endpoints — accessible to all logged-in users
    // -------------------------------------------------------------------------

    @GetMapping("/all")
    @ApiOperation(value = "获取所有启用字典（Key=dictCode，Value=字典项列表）")
    public ApiResult<Map<String, List<DictItemVO>>> getAllEnabled() {
        return ApiResult.success(sysDictService.getAllEnabled());
    }

    @GetMapping("/items/{dictCode}")
    @ApiOperation(value = "根据字典编码获取启用字典项（下拉框用，value/label）")
    public ApiResult<List<DictItemVO>> getItems(
            @ApiParam(value = "字典编码", required = true)
            @PathVariable String dictCode,
            @ApiParam(value = "为 true 时先清除 Redis 缓存再从数据库加载")
            @RequestParam(defaultValue = "false") boolean refresh) {
        if (refresh) {
            sysDictService.clearCache(dictCode);
        }
        return ApiResult.success(sysDictService.getItems(dictCode));
    }

    @GetMapping("/items/{dictCode}/manage")
    @ApiOperation(value = "管理端查询字典项全量（含 id/status，ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<List<SysDictItem>> getItemsForManage(
            @ApiParam(value = "字典编码", required = true) @PathVariable String dictCode) {
        List<SysDictItem> items = sysDictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictCode, dictCode)
                        .orderByAsc(SysDictItem::getSortNo)
        );
        return ApiResult.success(items);
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询字典分类列表（ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<IPage<SysDict>> page(@RequestBody SysDictPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .like(query.getDictCode() != null && !query.getDictCode().isEmpty(),
                        SysDict::getDictCode, query.getDictCode())
                .like(query.getDictName() != null && !query.getDictName().isEmpty(),
                        SysDict::getDictName, query.getDictName())
                .orderByAsc(SysDict::getSortNo);

        IPage<SysDict> pageResult = sysDictMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ApiResult.success(pageResult);
    }

    // -------------------------------------------------------------------------
    // Write endpoints — ADMIN only
    // -------------------------------------------------------------------------

    @PostMapping
    @ApiOperation(value = "新增字典分类（ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<String> createDict(@RequestBody Map<String, Object> body) {
        SysDict dict = new SysDict();
        dict.setDictCode((String) body.get("dictCode"));
        dict.setDictName((String) body.get("dictName"));
        dict.setDescription((String) body.get("description"));
        dict.setIsSystem(0);
        dict.setStatus(1);
        if (body.get("sortNo") != null) {
            dict.setSortNo(Integer.valueOf(body.get("sortNo").toString()));
        }

        sysDictMapper.insert(dict);
        return ApiResult.success("新增成功", dict.getId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新字典分类（ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> updateDict(
            @ApiParam(value = "字典分类ID", required = true) @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        LambdaUpdateWrapper<SysDict> wrapper = new LambdaUpdateWrapper<SysDict>()
                .eq(SysDict::getId, id)
                .set(body.containsKey("dictName"), SysDict::getDictName, body.get("dictName"))
                .set(body.containsKey("description"), SysDict::getDescription, body.get("description"))
                .set(body.containsKey("sortNo") && body.get("sortNo") != null,
                        SysDict::getSortNo, body.get("sortNo") != null ? Integer.valueOf(body.get("sortNo").toString()) : null)
                .set(body.containsKey("status"), SysDict::getStatus, body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null);

        sysDictMapper.update(null, wrapper);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "停用字典分类（set status=0；系统内置不可停用，ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> disableDict(
            @ApiParam(value = "字典分类ID", required = true) @PathVariable String id) {

        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "字典分类不存在");
        }
        if (Integer.valueOf(1).equals(dict.getIsSystem())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "系统内置字典不可删除");
        }

        LambdaUpdateWrapper<SysDict> wrapper = new LambdaUpdateWrapper<SysDict>()
                .eq(SysDict::getId, id)
                .set(SysDict::getStatus, 0);
        sysDictMapper.update(null, wrapper);

        sysDictService.clearCache(dict.getDictCode());
        return ApiResult.success();
    }

    @PostMapping("/items")
    @ApiOperation(value = "新增字典项（ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<String> createDictItem(@RequestBody Map<String, Object> body) {
        SysDictItem item = new SysDictItem();
        item.setDictCode((String) body.get("dictCode"));
        item.setItemValue((String) body.get("itemValue"));
        item.setItemLabel((String) body.get("itemLabel"));
        item.setColorTag((String) body.get("colorTag"));
        item.setRemark((String) body.get("remark"));
        item.setIsSystem(0);
        item.setStatus(1);
        if (body.get("sortNo") != null) {
            item.setSortNo(Integer.valueOf(body.get("sortNo").toString()));
        }

        sysDictItemMapper.insert(item);
        sysDictService.clearCache(item.getDictCode());
        return ApiResult.success("新增成功", item.getId());
    }

    @PutMapping("/items/{id}")
    @ApiOperation(value = "更新字典项（系统内置项不可修改 itemValue，ADMIN）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> updateDictItem(
            @ApiParam(value = "字典项ID", required = true) @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        SysDictItem item = sysDictItemMapper.selectById(id);
        if (item == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "字典项不存在");
        }

        LambdaUpdateWrapper<SysDictItem> wrapper = new LambdaUpdateWrapper<SysDictItem>()
                .eq(SysDictItem::getId, id)
                .set(body.containsKey("itemLabel"), SysDictItem::getItemLabel, body.get("itemLabel"))
                .set(body.containsKey("colorTag"), SysDictItem::getColorTag, body.get("colorTag"))
                .set(body.containsKey("sortNo") && body.get("sortNo") != null,
                        SysDictItem::getSortNo, body.get("sortNo") != null ? Integer.valueOf(body.get("sortNo").toString()) : null)
                .set(body.containsKey("remark"), SysDictItem::getRemark, body.get("remark"));

        // Non-system items may also update status and itemValue
        if (!Integer.valueOf(1).equals(item.getIsSystem())) {
            if (body.containsKey("status")) {
                wrapper.set(SysDictItem::getStatus, body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null);
            }
        }

        sysDictItemMapper.update(null, wrapper);
        sysDictService.clearCache(item.getDictCode());
        return ApiResult.success();
    }
}
