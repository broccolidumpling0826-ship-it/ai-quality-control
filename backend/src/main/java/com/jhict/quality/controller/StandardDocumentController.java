package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.PreparedClauseIndexCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardDocumentPageQuery;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/standard-documents")
@Api(tags = "标准源文档与条款")
public class StandardDocumentController {

    @Resource
    private StandardDocumentService standardDocumentService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询标准源文档")
    @SaCheckPermission("menu:standard-rag")
    public ApiResult<IPage<StandardDocumentVO>> pageDocuments(@RequestBody StandardDocumentPageQuery query) {
        return ApiResult.success(standardDocumentService.pageDocuments(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询标准源文档详情")
    @SaCheckPermission("menu:standard-rag")
    public ApiResult<StandardDocumentVO> getDocumentById(
            @ApiParam(value = "文档ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardDocumentService.getDocumentById(id));
    }

    @PostMapping("/clauses/page")
    @ApiOperation(value = "分页查询标准源条款")
    @SaCheckPermission("menu:standard-rag")
    public ApiResult<IPage<StandardClauseVO>> pageClauses(@RequestBody StandardClausePageQuery query) {
        return ApiResult.success(standardDocumentService.pageClauses(query));
    }

    @GetMapping("/clauses/{id}")
    @ApiOperation(value = "查询标准源条款详情")
    @SaCheckPermission("menu:standard-rag")
    public ApiResult<StandardClauseVO> getClauseById(
            @ApiParam(value = "条款ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardDocumentService.getClauseById(id));
    }

    @PostMapping("/clauses/batch")
    @ApiOperation(value = "按ID批量查询标准源条款")
    @SaCheckPermission("menu:standard-rag")
    public ApiResult<List<StandardClauseVO>> listClausesByIds(@RequestBody List<String> clauseIds) {
        return ApiResult.success(standardDocumentService.listClausesByIds(clauseIds));
    }

    @PostMapping("/clauses/index-prepared")
    @ApiOperation(value = "索引已准备标准源条款")
    @SaCheckPermission("standard:manage")
    public ApiResult<VectorIndexResponse> indexPreparedClauses(@RequestBody PreparedClauseIndexCmd cmd) {
        return ApiResult.success(standardDocumentService.indexPreparedClauses(cmd));
    }
}
