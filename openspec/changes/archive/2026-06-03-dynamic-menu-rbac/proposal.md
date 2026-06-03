## Why

当前前端菜单在 `MainLayout.vue` 中硬编码，路由在 `router/index.ts` 中静态注册，两套配置独立维护且未与后端权限联动。随着业务页面增多、角色差异化需求增强（质量工程师、质检主管、销售经理等），硬编码方式导致菜单与权限难以统一管理，也无法由管理员在运行时调整菜单结构。规格文档中 `GET /auth/user/info` 已定义 `permissions[]` 响应，但尚未实现。

## What Changes

- 新增数据库表：`sys_menu`、`sys_role`、`sys_permission` 及关联表 `sys_role_menu`、`sys_role_permission`、`sys_user_role`
- 新增后端菜单/角色/权限管理 API，登录后按用户角色返回过滤后的菜单树
- 扩展 `GET /auth/user/info` 返回 `roles[]` 和 `permissions[]`（对齐现有 auth 契约）
- 改造 Sa-Token `StpInterfaceImpl`，从数据库加载用户权限列表
- 前端登录后动态注册路由（`router.addRoute`）并从 API 渲染侧栏，替代硬编码导航
- 新增 `v-permission` 指令，支持按钮级权限控制
- 新增管理员页面：菜单管理、角色管理；扩展账号管理支持多角色分配
- 提供 seed 脚本，将现有 19+ 条路由导入为初始菜单数据
- 支持菜单类型扩展：`DIR`（目录）、`MENU`（内部页）、`HIDDEN`（隐藏路由）、`LINK`（外链）、`IFRAME`（内嵌页）
- **BREAKING**：用户模型从单角色字段扩展为多角色关联；前端路由注册时机从应用启动改为登录后动态注入

## Capabilities

### New Capabilities

- `dynamic-menu`: 可配置的动态菜单系统，含菜单树 CRUD、按角色过滤、前端动态路由与侧栏渲染
- `rbac`: 完整 RBAC 权限体系，含角色/权限点管理、角色-菜单/权限分配、Sa-Token 集成与按钮级权限

### Modified Capabilities

- （无）`openspec/specs/` 目录尚无存量 spec；auth 契约变更将在 `rbac` capability spec 中作为需求覆盖

## Impact

- **数据库**：新增 6 张 RBAC 相关表；`sys_user.role` 保留作兼容字段，新增 `sys_user_role` 关联
- **后端**：新增 Menu/Role/Permission Entity、Mapper、Service、Controller；改造 `StpInterfaceImpl`、`AuthServiceImpl`、`UserManageController`
- **前端**：改造 `MainLayout.vue`、`router/index.ts`、`store/auth.ts`、`main.ts`；新增 `store/menu.ts`、`router/dynamic.ts`、`directives/permission.ts`、管理页面
- **缓存**：用户菜单树 Redis 缓存（TTL 5min），菜单/角色变更时失效
- **迁移**：需执行 DDL + seed 脚本；现有 ADMIN 用户和 5 种业务角色自动导入
