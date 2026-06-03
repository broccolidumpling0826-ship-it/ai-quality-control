package com.jhict.quality.service.api;

import com.jhict.quality.vo.MenuTreeVO;

import java.util.List;

public interface MenuService {

    List<MenuTreeVO> getUserMenuTree(String userNo);

    void evictUserMenuCache(String userNo);

    void evictAllMenuCache();
}
