package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.PrfKpiTarget;
import com.example.query.PrfKpiTargetQuery;
import com.example.service.PrfKpiTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * KPI目标设置 控制器
 */
@RestController
@RequestMapping("/api/kpi-target")
public class PrfKpiTargetController {

    @Autowired
    private PrfKpiTargetService prfKpiTargetService;

    /**
     * 分页查询KPI目标列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> queryPage(PrfKpiTargetQuery query) {
        IPage<PrfKpiTarget> page = prfKpiTargetService.queryPage(query);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", page);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID查询KPI目标详情
     *
     * @param id 主键ID
     * @return KPI目标详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        PrfKpiTarget entity = prfKpiTargetService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (entity != null) {
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", entity);
        } else {
            result.put("code", 404);
            result.put("message", "记录不存在");
            result.put("data", null);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增KPI目标
     *
     * @param entity KPI目标实体
     * @return 操作结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody PrfKpiTarget entity) {
        boolean success = prfKpiTargetService.add(entity);
        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "新增成功" : "新增失败");
        result.put("data", success ? entity : null);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改KPI目标
     *
     * @param id     主键ID
     * @param entity KPI目标实体
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id,
                                                      @RequestBody PrfKpiTarget entity) {
        entity.setId(id);
        boolean success = prfKpiTargetService.update(entity);
        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "修改成功" : "修改失败");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除KPI目标
     *
     * @param id 主键ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> remove(@PathVariable String id) {
        boolean success = prfKpiTargetService.remove(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "删除失败");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
}
