## 1. 数据库与 Seed 脚本

- [x] 1.1 编写 `backend/scripts/init-menu-rbac.sql`：创建 sys_role、sys_permission、sys_menu、sys_role_menu、sys_role_permission、sys_user_role 六张表
- [x] 1.2 编写 seed 数据：从 USER_ROLE 字典导入 5 种角色到 sys_role
- [x] 1.3 编写 seed 数据：从现有 router/index.ts 导入 19+ 条路由到 sys_menu（含 HIDDEN 类型的 form/detail 页）
- [x] 1.4 编写 seed 数据：创建业务权限点（standard:manage、inspection:void、concession:approve 等）及默认角色-权限映射
- [x] 1.5 编写用户迁移脚本：将 sys_user.role 同步写入 sys_user_role
- [x] 1.6 编写默认角色-菜单分配（ADMIN 全部菜单，业务角色按当前访问模式分配）

## 2. 后端 Entity 与 Mapper（Phase 1）

- [x] 2.1 创建 SysRole、SysPermission、SysMenu Entity 及对应 Mapper/XML
- [x] 2.2 创建 SysRoleMenu、SysRolePermission、SysUserRole 关联 Entity 及 Mapper
- [x] 2.3 创建 MenuVO、MenuTreeVO、RoleVO、PermissionVO 等视图对象

## 3. 后端菜单 API（Phase 1）

- [x] 3.1 实现 MenuService：构建用户菜单树（按角色过滤、按 sort_order 排序）
- [x] 3.2 实现 MenuController：`GET /api/v1/menus/user-tree` 接口
- [x] 3.3 实现菜单树 Redis 缓存（key=menu:tree:{userNo}，TTL 5min）及失效方法
- [x] 3.4 扩展 AuthServiceImpl / UserInfoVO：`GET /auth/user/info` 返回 roles[] 和 permissions[]

## 4. 前端动态路由与侧栏（Phase 1）

- [x] 4.1 创建 `frontend/src/store/menu.ts`：存储菜单树、提供 fetchMenuTree action
- [x] 4.2 创建 `frontend/src/router/dynamic.ts`：import.meta.glob 组件映射 + buildRoutes 函数
- [x] 4.3 改造 `store/auth.ts`：登录后并行请求 user/info 和 menus/user-tree，触发动态路由注册
- [x] 4.4 改造 `main.ts`：有 token 时启动前先拉菜单再 mount，解决刷新 404
- [x] 4.5 改造 `router/index.ts`：移除静态业务 children，保留 login/404/MainLayout 壳 + 升级路由守卫
- [x] 4.6 创建 `SidebarMenuItem.vue` 递归组件，支持 DIR/MENU/LINK 渲染
- [x] 4.7 改造 `MainLayout.vue`：删除硬编码 NavItem 数组，从 menuStore 渲染侧栏
- [x] 4.8 验证：不同角色登录看到不同菜单；HIDDEN 路由可直达但不在侧栏显示

## 5. 后端管理 API（Phase 2）

- [x] 5.1 实现 MenuManageService：菜单 CRUD、批量排序、删除校验（有子节点不可删）
- [x] 5.2 实现 MenuManageController：`/api/v1/admin/menus` CRUD 接口（@SaCheckRole ADMIN 过渡期）
- [x] 5.3 实现 RoleManageService 及 RoleManageController：角色 CRUD
- [x] 5.4 实现 `PUT /admin/roles/{id}/menus` 和 `PUT /admin/roles/{id}/permissions` 分配接口
- [x] 5.5 菜单/角色变更时触发 Redis 缓存失效

## 6. 前端管理页面（Phase 2）

- [x] 6.1 创建 `frontend/src/api/menu.ts` 和 `frontend/src/api/role.ts` API 模块
- [x] 6.2 创建 `views/admin/menus/index.vue`：树形表格 CRUD、拖拽排序、component 路径下拉（glob 扫描）
- [x] 6.3 创建 `views/admin/roles/index.vue`：角色列表 + 抽屉分配菜单树和权限勾选
- [x] 6.4 将菜单管理和角色管理页面加入 seed 菜单数据（admin 区块）

## 7. RBAC 权限体系（Phase 3）

- [x] 7.1 改造 StpInterfaceImpl：getRoleList 从 sys_user_role 查询；getPermissionList 从 sys_role_permission 聚合
- [x] 7.2 扩展 UserManageController：支持多角色分配 `PUT /admin/users/{id}/roles`；保留 sys_user.role 同步
- [x] 7.3 改造 `views/admin/users/index.vue`：角色选择改为多选
- [x] 7.4 创建 `frontend/src/directives/permission.ts`：v-permission 指令
- [x] 7.5 在 main.ts 注册 v-permission 指令
- [x] 7.6 升级路由守卫：检查目标路由 perm_code 是否在用户 permissions 中
- [x] 7.7 在关键业务页面添加 v-permission（检验作废、改判审批、让步审批、标准库编辑等）
- [x] 7.8 在管理 API 上添加 @SaCheckPermission 注解（与 @SaCheckRole 并行）

## 8. 扩展菜单类型与迁移（Phase 4）

- [x] 8.1 实现 LINK 菜单类型：点击新窗口打开 externalUrl
- [x] 8.2 创建 iframe 容器页 `views/iframe/index.vue`，实现 IFRAME 菜单类型
- [x] 8.3 逐步将 Controller @SaCheckRole 迁移为 @SaCheckPermission
- [x] 8.4 清理 MainLayout.vue 中残留的 dashboard badge 硬编码（可选配置化 meta_json.badgeApi）
- [x] 8.5 编写集成测试：菜单 API 角色过滤、权限校验、动态路由注册
