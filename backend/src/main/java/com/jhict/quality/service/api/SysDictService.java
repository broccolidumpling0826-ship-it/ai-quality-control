package com.jhict.quality.service.api;

import com.jhict.quality.vo.DictItemVO;

import java.util.List;
import java.util.Map;

public interface SysDictService {

    /**
     * 获取所有启用字典（以 dictCode 为 key，字典项列表为 value）
     *
     * @return 字典 Map
     */
    Map<String, List<DictItemVO>> getAllEnabled();

    /**
     * 根据字典编码获取字典项列表
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<DictItemVO> getItems(String dictCode);

    /**
     * 清除指定字典编码的 Redis 缓存
     *
     * @param dictCode 字典编码
     */
    void clearCache(String dictCode);
}
