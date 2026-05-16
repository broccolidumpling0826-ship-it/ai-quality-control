package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.entity.SysDict;
import com.jhict.quality.entity.SysDictItem;
import com.jhict.quality.mapper.SysDictItemMapper;
import com.jhict.quality.mapper.SysDictMapper;
import com.jhict.quality.service.api.SysDictService;
import com.jhict.quality.vo.DictItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysDictServiceImpl implements SysDictService {

    private static final String CACHE_KEY_PREFIX = "dict:items:";
    private static final long CACHE_TTL_SECONDS = 600L;

    @Resource
    private SysDictItemMapper sysDictItemMapper;

    @Resource
    private SysDictMapper sysDictMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DictItemVO> getItems(String dictCode) {
        String cacheKey = CACHE_KEY_PREFIX + dictCode;

        // 先查 Redis 缓存
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<DictItemVO>>() {});
            } catch (Exception e) {
                log.warn("字典缓存反序列化失败，dictCode={}，将重新查询DB", dictCode, e);
            }
        }

        // 缓存未命中，查 DB
        List<SysDictItem> items = sysDictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictCode, dictCode)
                        .eq(SysDictItem::getStatus, 1)
                        .orderByAsc(SysDictItem::getSortNo)
        );

        List<DictItemVO> result = items.stream()
                .map(item -> DictItemVO.builder()
                        .value(item.getItemValue())
                        .label(item.getItemLabel())
                        .colorTag(item.getColorTag())
                        .sortNo(item.getSortNo())
                        .build())
                .collect(Collectors.toList());

        // 结果写入 Redis
        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("字典缓存写入Redis失败，dictCode={}", dictCode, e);
        }

        return result;
    }

    @Override
    public Map<String, List<DictItemVO>> getAllEnabled() {
        // 查所有启用的字典分类
        List<SysDict> dicts = sysDictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSortNo)
        );

        if (CollectionUtils.isEmpty(dicts)) {
            return Collections.emptyMap();
        }

        // 批量调用 getItems 组装 Map
        return dicts.stream()
                .collect(Collectors.toMap(
                        SysDict::getDictCode,
                        dict -> getItems(dict.getDictCode()),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public void clearCache(String dictCode) {
        String cacheKey = CACHE_KEY_PREFIX + dictCode;
        stringRedisTemplate.delete(cacheKey);
        log.info("已清除字典缓存，dictCode={}", dictCode);
    }
}
