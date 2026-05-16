package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.OaLeaveRecord;
import com.example.mapper.OaLeaveRecordMapper;
import com.example.query.OaLeaveRecordQuery;
import com.example.service.OaLeaveRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 请假记录 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class OaLeaveRecordServiceImpl implements OaLeaveRecordService {

    private final OaLeaveRecordMapper oaLeaveRecordMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<OaLeaveRecord> page(OaLeaveRecordQuery query) {
        Page<OaLeaveRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        return oaLeaveRecordMapper.selectPageByQuery(page, query);
    }

    @Override
    public OaLeaveRecord getById(String id) {
        return oaLeaveRecordMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OaLeaveRecord record) {
        // 生成主键
        if (record.getId() == null || record.getId().isEmpty()) {
            record.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        // 默认有效
        if (record.getIsValid() == null) {
            record.setIsValid(1);
        }
        // 记录创建时间
        record.setCreateDateTime(LocalDateTime.now().format(FORMATTER));
        oaLeaveRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OaLeaveRecord record) {
        // 记录修改时间
        record.setUpdateDateTime(LocalDateTime.now().format(FORMATTER));
        oaLeaveRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        // 逻辑删除：将 IS_VALID 置为 0
        LambdaUpdateWrapper<OaLeaveRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OaLeaveRecord::getId, id)
               .set(OaLeaveRecord::getIsValid, 0)
               .set(OaLeaveRecord::getUpdateDateTime, LocalDateTime.now().format(FORMATTER));
        oaLeaveRecordMapper.update(null, wrapper);
    }
}
