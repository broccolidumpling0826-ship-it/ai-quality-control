package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardConflict;
import com.jhict.quality.entity.QcStandardDocumentChunk;
import com.jhict.quality.enums.ConflictStatus;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardConflictMapper;
import com.jhict.quality.mapper.QcStandardDocumentChunkMapper;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.vo.CitationVO;
import com.jhict.quality.vo.StandardConflictVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StandardConflictServiceImpl implements StandardConflictService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcStandardConflictMapper conflictMapper;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private QcStandardDocumentChunkMapper chunkMapper;

    @Override
    public IPage<StandardConflictVO> page(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<QcStandardConflict> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(QcStandardConflict::getConflictStatus, status);
        }
        wrapper.orderByDesc(QcStandardConflict::getCreateDateTime);
        IPage<QcStandardConflict> page = conflictMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<StandardConflictVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public StandardConflictVO getById(String id) {
        QcStandardConflict conflict = conflictMapper.selectById(id);
        if (conflict == null) {
            throw new ServiceException("标准冲突记录不存在");
        }
        return toVo(conflict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(String id, StandardConflictResolveCmd cmd, String userNo) {
        QcStandardConflict conflict = conflictMapper.selectById(id);
        if (conflict == null) {
            throw new ServiceException("标准冲突记录不存在");
        }
        conflict.setConflictStatus(cmd.getConflictStatus());
        conflict.setResolutionNote(cmd.getResolutionNote());
        conflict.setResolvedBy(userNo);
        conflict.setResolvedTime(LocalDateTime.now().format(FORMATTER));
        conflict.setUpdateDateTime(conflict.getResolvedTime());
        conflictMapper.updateById(conflict);
    }

    private StandardConflictVO toVo(QcStandardConflict conflict) {
        StandardConflictVO vo = new StandardConflictVO();
        vo.setId(conflict.getId());
        vo.setVariety(conflict.getVariety());
        vo.setGrade(conflict.getGrade());
        vo.setIndicatorId(conflict.getIndicatorId());
        vo.setStandardIdA(conflict.getStandardIdA());
        vo.setStandardIdB(conflict.getStandardIdB());
        vo.setLimitAUpper(conflict.getLimitAUpper());
        vo.setLimitALower(conflict.getLimitALower());
        vo.setLimitBUpper(conflict.getLimitBUpper());
        vo.setLimitBLower(conflict.getLimitBLower());
        vo.setConflictStatus(conflict.getConflictStatus());
        vo.setResolutionNote(conflict.getResolutionNote());

        QcIndicatorItem indicator = indicatorItemMapper.selectById(conflict.getIndicatorId());
        if (indicator != null) {
            vo.setIndicatorName(indicator.getIndicatorName());
        }
        Map<String, QcQualityStandard> stdMap = qualityStandardMapper.selectBatchIds(
                Arrays.asList(conflict.getStandardIdA(), conflict.getStandardIdB()))
                .stream().collect(Collectors.toMap(QcQualityStandard::getId, s -> s, (a, b) -> a));
        QcQualityStandard stdA = stdMap.get(conflict.getStandardIdA());
        QcQualityStandard stdB = stdMap.get(conflict.getStandardIdB());
        if (stdA != null) {
            vo.setStandardNameA(StringUtils.hasText(stdA.getStandardName()) ? stdA.getStandardName() : stdA.getStandardCode());
        }
        if (stdB != null) {
            vo.setStandardNameB(StringUtils.hasText(stdB.getStandardName()) ? stdB.getStandardName() : stdB.getStandardCode());
        }
        vo.setCitations(loadCitations(conflict));
        return vo;
    }

    private List<CitationVO> loadCitations(QcStandardConflict conflict) {
        List<CitationVO> citations = new ArrayList<>();
        for (String stdId : Arrays.asList(conflict.getStandardIdA(), conflict.getStandardIdB())) {
            List<QcStandardDocumentChunk> chunks = chunkMapper.selectList(
                    new LambdaQueryWrapper<QcStandardDocumentChunk>()
                            .eq(QcStandardDocumentChunk::getStandardId, stdId)
                            .last("LIMIT 1"));
            if (!chunks.isEmpty()) {
                QcStandardDocumentChunk chunk = chunks.get(0);
                CitationVO c = new CitationVO();
                c.setChunkId(chunk.getId());
                c.setStandardId(stdId);
                c.setSectionRef(chunk.getSectionRef());
                c.setHighlightText(chunk.getChunkText().length() > 150
                        ? chunk.getChunkText().substring(0, 150) + "..." : chunk.getChunkText());
                citations.add(c);
            }
        }
        return citations;
    }
}
