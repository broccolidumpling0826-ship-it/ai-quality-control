## Context

质量控制系统（ai-quality-control）采用 Vue 3 + Spring Boot 2.7 前后端分离架构。当前前端菜单在 `MainLayout.vue` 硬编码，路由在 `router/index.ts` 静态注册，二者独立维护。后端使用 Sa-Token 做登录拦截和 `@SaCheckRole` 角色校验，用户角色存于 `sys_user.role` 单字段，无菜单/权限表。`StpInterfaceImpl.getPermissionList()` 对非 ADMIN 用户返回空列表，前端 `permissions[]` 契约未实现。

**约束：**
- MySQL 5.7.43（无 CTE/窗口函数）
- Redis 8.0.5 已有字典缓存模式可复用
- 现有 5 种业务角色（QUALITY_ENGINEER 等）须保持兼容
- ADMIN 特权模式（通过任意 `@SaCheckRole`）须保留

## Goals / Non-Goals

**Goals:**
- 菜单数据存 DB，管理员可 CRUD 配置菜单树（名称、路径、图标、排序、显隐）
- 登录后 API 返回当前用户可见菜单树，前端动态注册路由并渲染侧栏
- 完整 RBAC：角色、权限点、角色-菜单/权限分配、用户多角色
- 按钮级权限：`v-permission` 指令 + `@SaCheckPermission`
- 支持菜单类型 DIR / MENU / HIDDEN / LINK / IFRAME
- seed 脚本导入现有路由为初始菜单，平滑迁移

**Non-Goals:**
- 低代码页面构建器（不能通过配置创建全新 Vue 组件，组件须预先存在于代码库）
- 替换 Sa-Token 为其他鉴权框架
- 一次性将所有 Service 层 `AuthUtils.hasRole` 改为 permission 码（Phase 4 渐进迁移）
- 多租户菜单隔离

## Decisions

### D-1：菜单驱动路由，而非路由驱动菜单

**选择：** 以 `sys_menu` 为侧栏和动态路由的唯一数据源。

**理由：** 侧栏分组（标准库/检验与判定/质量流程等）是 UI 概念，不一定 1:1 对应路由层级；菜单可配置 `visible=0` 的 HIDDEN 类型注册路由但不展示（如 form/detail 页）。

**替代方案：** 从 `router/index.ts` meta 过滤生成菜单 — 拒绝，无法运行时配置且分组不灵活。

### D-2：组件加载用 `import.meta.glob` 预扫描

**选择：** 菜单 `component` 字段存相对路径（如 `inspection/index`），前端通过 Vite glob 映射到 `@/views/inspection/index.vue`。

**理由：** Vite 要求动态 import 路径可静态分析；存完整 URL 或任意字符串会导致构建失败。

**替代方案：** 后端返回组件源码 — 安全风险，拒绝。

### D-3：权限模型 — 菜单关联 perm_code，按钮/API 独立权限点

**选择：**
- 每个 MENU 类型菜单绑定一个 `perm_code`（如 `menu:inspection`）
- 按钮/API 权限独立编码（如 `inspection:void`、`concession:approve`）
- 角色通过 `sys_role_menu` 和 `sys_role_permission` 分别授权

**理由：** 菜单可见性与操作权限解耦；用户可能看到列表页但无删除按钮。

### D-4：用户多角色 + 兼容单字段

**选择：** 新增 `sys_user_role` 多对多表；保留 `sys_user.role` 作为「主角色」兼容字段（迁移时写入首条关联）。

**理由：** 避免一次性改动所有读取 `sys_user.role` 的代码；`StpInterfaceImpl.getRoleList()` 逐步改为查关联表。

### D-5：ADMIN 特权保持不变

**选择：** ADMIN 用户的 `getRoleList()` 返回全部业务角色，`getPermissionList()` 返回 `["*"]`。

**理由：** 现有 Service 层审批流依赖 ADMIN 跳过角色校验；改动影响面过大。

### D-6：缓存策略

**选择：** 用户菜单树 Redis 缓存 key=`menu:tree:{userNo}`，TTL 5min；菜单/角色/权限变更时按前缀失效或主动删除。

**理由：** 每次页面刷新都查 DB 构建树成本高；5min TTL 与字典缓存（10min）量级一致。

### D-7：分四阶段交付

| Phase | 范围 |
|-------|------|
| 1 | DDL + seed + 菜单 API + 动态路由/侧栏 |
| 2 | 菜单/角色管理 Admin UI + 缓存失效 |
| 3 | 权限点 + StpInterfaceImpl + v-permission + 多角色 |
| 4 | LINK/IFRAME + @SaCheckPermission 迁移 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 刷新页面时动态路由未注册导致 404 | `main.ts` 启动时：有 token 则先拉菜单再 mount；路由守卫中未就绪时显示 loading |
| 菜单 component 路径手输错误 | 管理界面 component 下拉从 glob 扫描结果生成 |
| 双轨维护过渡期混乱 | Phase 1 完成后删除 MainLayout 硬编码；静态 children 路由全部移除 |
| Service 层角色硬编码与 RBAC 不一致 | Phase 3 前不影响；逐步映射为 permission 码 |
| seed 脚本与代码路由漂移 | 管理界面显示「未注册组件」警告；CI 可选校验脚本 |

## Migration Plan

1. **DDL 执行**：`init-menu-rbac.sql` 创建 6 张表
2. **Seed 数据**：从现有 `router/index.ts` 导入菜单；从 `USER_ROLE` 字典导入角色；预置权限点
3. **用户迁移**：`INSERT INTO sys_user_role SELECT id, role_id FROM sys_user JOIN sys_role ON role_code = role`
4. **部署顺序**：先后端 API → 再前端动态路由（向后兼容：旧前端仍可用静态路由直到前端部署）
5. **回滚**：保留 `sys_user.role` 字段；回滚前端至静态路由版本即可恢复

## Open Questions

- Dashboard 徽章数量（待判/复检/让步计数）是否纳入 `meta_json.badgeApi` 配置？建议 Phase 1 保留 dashboard 专用逻辑，Phase 2 再配置化。
- IFRAME 菜单是否需要 CSP 白名单？建议 Phase 4 实现时增加 `allowedOrigins` 配置字段。
