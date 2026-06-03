package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.entity.SysMenu;
import com.jhict.quality.entity.SysRoleMenu;
import com.jhict.quality.mapper.SysMenuMapper;
import com.jhict.quality.mapper.SysRoleMenuMapper;
import com.jhict.quality.service.api.MenuService;
import com.jhict.quality.service.api.RbacQueryService;
import com.jhict.quality.vo.MenuTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private static final String CACHE_KEY_PREFIX = "menu:tree:";
    private static final long CACHE_TTL_SECONDS = 300L;

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Resource
    private RbacQueryService rbacQueryService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MenuTreeVO> getUserMenuTree(String userNo) {
        String cacheKey = CACHE_KEY_PREFIX + userNo;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<MenuTreeVO>>() {});
            } catch (Exception e) {
                log.warn("菜单缓存反序列化失败，userNo={}", userNo, e);
            }
        }

        List<MenuTreeVO> tree = buildUserMenuTree(userNo);
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(tree),
                    CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("菜单缓存写入失败，userNo={}", userNo, e);
        }
        return tree;
    }

    private List<MenuTreeVO> buildUserMenuTree(String userNo) {
        List<String> roleCodes = rbacQueryService.getUserRoleCodes(userNo);
        List<String> roleIds = rbacQueryService.getUserRoleIds(userNo);

        Set<String> allowedMenuIds;
        if (roleCodes.contains(AuthUtils.ROLE_ADMIN)) {
            allowedMenuIds = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1)
            ).stream().map(SysMenu::getId).collect(Collectors.toSet());
        } else if (CollectionUtils.isEmpty(roleIds)) {
            allowedMenuIds = new HashSet<>();
        } else {
            allowedMenuIds = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
            ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        }

        if (allowedMenuIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> expandedMenuIds = expandWithAncestorDirs(allowedMenuIds);

        List<SysMenu> menus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .in(SysMenu::getId, expandedMenuIds)
                        .orderByAsc(SysMenu::getSortOrder)
        );

        return buildTree(menus);
    }

    /** 补齐父级 DIR 节点，否则子菜单会被提升为根节点，侧栏失去层次结构 */
    private Set<String> expandWithAncestorDirs(Set<String> menuIds) {
        Set<String> expanded = new HashSet<>(menuIds);
        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1)
        );
        Map<String, SysMenu> menuById = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String id : new ArrayList<>(expanded)) {
                SysMenu menu = menuById.get(id);
                if (menu == null) {
                    continue;
                }
                String parentId = menu.getParentId();
                if (StringUtils.hasText(parentId) && !"0".equals(parentId) && expanded.add(parentId)) {
                    changed = true;
                }
            }
        }
        return expanded;
    }

    static List<MenuTreeVO> buildTree(List<SysMenu> menus) {
        Map<String, MenuTreeVO> nodeMap = new HashMap<>();
        for (SysMenu menu : menus) {
            nodeMap.put(menu.getId(), toVo(menu));
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            MenuTreeVO node = nodeMap.get(menu.getId());
            String parentId = menu.getParentId();
            if (!StringUtils.hasText(parentId) || "0".equals(parentId) || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    private static void sortTree(List<MenuTreeVO> nodes) {
        nodes.sort(Comparator.comparingInt(n -> n.getSortOrder() != null ? n.getSortOrder() : 0));
        for (MenuTreeVO node : nodes) {
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                sortTree(node.getChildren());
            }
        }
    }

    static MenuTreeVO toVo(SysMenu menu) {
        return MenuTreeVO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuType(menu.getMenuType())
                .menuName(menu.getMenuName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .routeName(menu.getRouteName())
                .icon(menu.getIcon())
                .permCode(menu.getPermCode())
                .visible(menu.getVisible())
                .sortOrder(menu.getSortOrder())
                .metaJson(menu.getMetaJson())
                .build();
    }

    @Override
    public void evictUserMenuCache(String userNo) {
        if (StringUtils.hasText(userNo)) {
            stringRedisTemplate.delete(CACHE_KEY_PREFIX + userNo);
        }
    }

    @Override
    public void evictAllMenuCache() {
        Set<String> keys = stringRedisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            stringRedisTemplate.delete(keys);
        }
    }
}
