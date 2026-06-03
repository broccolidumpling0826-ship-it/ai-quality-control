package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.MenuSaveCmd;
import com.jhict.quality.dto.MenuSortCmd;
import com.jhict.quality.entity.SysMenu;
import com.jhict.quality.mapper.SysMenuMapper;
import com.jhict.quality.service.api.MenuManageService;
import com.jhict.quality.service.api.MenuService;
import com.jhict.quality.vo.MenuTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MenuManageServiceImpl implements MenuManageService {

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Resource
    private MenuService menuService;

    @Override
    public List<MenuTreeVO> getFullMenuTree() {
        List<SysMenu> menus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder)
        );
        return MenuServiceImpl.buildTree(menus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeVO createMenu(MenuSaveCmd cmd) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(cmd, menu);
        if (menu.getParentId() == null) {
            menu.setParentId("0");
        }
        sysMenuMapper.insert(menu);
        menuService.evictAllMenuCache();
        return MenuServiceImpl.toVo(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeVO updateMenu(String id, MenuSaveCmd cmd) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "菜单不存在");
        }
        BeanUtils.copyProperties(cmd, menu);
        menu.setId(id);
        sysMenuMapper.updateById(menu);
        menuService.evictAllMenuCache();
        return MenuServiceImpl.toVo(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String id) {
        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)
        );
        if (childCount != null && childCount > 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "存在子菜单，请先删除子菜单");
        }
        sysMenuMapper.deleteById(id);
        menuService.evictAllMenuCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSort(List<MenuSortCmd> cmds) {
        if (CollectionUtils.isEmpty(cmds)) {
            return;
        }
        for (MenuSortCmd cmd : cmds) {
            SysMenu menu = new SysMenu();
            menu.setId(cmd.getId());
            menu.setSortOrder(cmd.getSortOrder());
            sysMenuMapper.updateById(menu);
        }
        menuService.evictAllMenuCache();
    }
}
