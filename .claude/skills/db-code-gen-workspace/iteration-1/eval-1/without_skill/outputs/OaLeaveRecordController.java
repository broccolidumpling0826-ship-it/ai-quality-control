package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.OaLeaveRecord;
import com.example.query.OaLeaveRecordQuery;
import com.example.service.OaLeaveRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 请假记录 Controller
 */
@RestController
@RequestMapping("/api/oa/leave-record")
@RequiredArgsConstructor
public class OaLeaveRecordController {

    private final OaLeaveRecordService oaLeaveRecordService;

    /**
     * 分页查询请假记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> page(OaLeaveRecordQuery query) {
        IPage<OaLeaveRecord> pageResult = oaLeaveRecordService.page(query);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", pageResult);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据主键查询请假记录详情
     *
     * @param id 主键
     * @return 请假记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        OaLeaveRecord record = oaLeaveRecordService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", record);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增请假记录
     *
     * @param record 请假记录
     * @return 操作结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody OaLeaveRecord record) {
        oaLeaveRecordService.save(record);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "新增成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 修改请假记录
     *
     * @param id     主键
     * @param record 请假记录
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id,
                                                      @RequestBody OaLeaveRecord record) {
        record.setId(id);
        oaLeaveRecordService.update(record);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "修改成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 根据主键删除请假记录（逻辑删除）
     *
     * @param id 主键
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable String id) {
        oaLeaveRecordService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "删除成功");
        return ResponseEntity.ok(result);
    }
}
