package com.jhict.quality.service.api;

import com.jhict.quality.dto.MenuSaveCmd;
import com.jhict.quality.dto.MenuSortCmd;
import com.jhict.quality.vo.MenuTreeVO;

import java.util.List;

public interface MenuManageService {

    List<MenuTreeVO> getFullMenuTree();

    MenuTreeVO createMenu(MenuSaveCmd cmd);

    MenuTreeVO updateMenu(String id, MenuSaveCmd cmd);

    void deleteMenu(String id);

    void batchSort(List<MenuSortCmd> cmds);
}
