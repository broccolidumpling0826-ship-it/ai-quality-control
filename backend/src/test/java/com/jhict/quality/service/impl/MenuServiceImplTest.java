package com.jhict.quality.service.impl;

import com.jhict.quality.entity.SysMenu;
import com.jhict.quality.vo.MenuTreeVO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuServiceImplTest {

    @Test
    void buildTree_shouldNestChildrenUnderParent() {
        SysMenu root = menu("m1", "0", "DIR", "根", 1);
        SysMenu child = menu("m2", "m1", "MENU", "子页", 2);

        List<MenuTreeVO> tree = MenuServiceImpl.buildTree(Arrays.asList(root, child));

        assertEquals(1, tree.size());
        assertEquals("m1", tree.get(0).getId());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("m2", tree.get(0).getChildren().get(0).getId());
    }

    @Test
    void buildTree_shouldSortBySortOrder() {
        SysMenu second = menu("m2", "0", "MENU", "B", 20);
        SysMenu first = menu("m1", "0", "MENU", "A", 10);

        List<MenuTreeVO> tree = MenuServiceImpl.buildTree(Arrays.asList(second, first));

        assertEquals("m1", tree.get(0).getId());
        assertEquals("m2", tree.get(1).getId());
    }

    @Test
    void toVo_shouldMapFields() {
        SysMenu menu = menu("x1", "0", "MENU", "测试", 5);
        menu.setPath("dashboard");
        menu.setPermCode("menu:dashboard");

        MenuTreeVO vo = MenuServiceImpl.toVo(menu);

        assertEquals("x1", vo.getId());
        assertEquals("dashboard", vo.getPath());
        assertEquals("menu:dashboard", vo.getPermCode());
        assertTrue(vo.getChildren().isEmpty());
    }

    private static SysMenu menu(String id, String parentId, String type, String name, int sort) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setParentId(parentId);
        m.setMenuType(type);
        m.setMenuName(name);
        m.setSortOrder(sort);
        m.setStatus(1);
        m.setVisible(1);
        return m;
    }
}
