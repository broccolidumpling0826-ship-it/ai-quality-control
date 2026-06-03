## ADDED Requirements

### Requirement: Role and permission data model

The system SHALL maintain roles in `sys_role`, permissions in `sys_permission`, and association tables `sys_role_menu`, `sys_role_permission`, and `sys_user_role` for many-to-many relationships.

#### Scenario: Role seeded from existing USER_ROLE dictionary

- **WHEN** the RBAC seed script is executed
- **THEN** the system SHALL create roles matching all entries in the USER_ROLE dictionary (QUALITY_ENGINEER, QUALITY_SUPERVISOR, QUALITY_MANAGER, SALES_MANAGER, ADMIN)

#### Scenario: User assigned multiple roles

- **WHEN** an ADMIN assigns roles [QUALITY_ENGINEER, QUALITY_SUPERVISOR] to a user via `PUT /api/v1/admin/users/{id}/roles`
- **THEN** the system SHALL persist both role associations in sys_user_role and the user's effective permissions SHALL be the union of both roles' permissions

### Requirement: User info returns roles and permissions

The system SHALL extend `GET /api/v1/auth/user/info` to return `roles[]` (array of role codes) and `permissions[]` (array of permission codes), aligning with the existing auth contract specification.

#### Scenario: Non-admin user receives role-specific permissions

- **WHEN** a QUALITY_SUPERVISOR user requests user info
- **THEN** the response SHALL include `roles: ["QUALITY_SUPERVISOR"]` and `permissions` containing all permission codes assigned to that role via sys_role_permission

#### Scenario: Admin user receives wildcard permission

- **WHEN** an ADMIN user requests user info
- **THEN** the response SHALL include `permissions: ["*"]` indicating full access

### Requirement: Sa-Token permission provider loads from database

The system SHALL implement `StpInterfaceImpl.getPermissionList()` to query sys_role_permission via the user's roles, and `getRoleList()` to query sys_user_role (with ADMIN returning all business roles).

#### Scenario: Permission check passes for authorized user

- **WHEN** a Controller method annotated with `@SaCheckPermission("inspection:void")` is called by a user whose role has that permission
- **THEN** the request SHALL proceed successfully

#### Scenario: Permission check rejects unauthorized user

- **WHEN** a Controller method annotated with `@SaCheckPermission("inspection:void")` is called by a user without that permission
- **THEN** the system SHALL return HTTP 403 with ApiResult code indicating permission denied

### Requirement: Frontend button-level permission control

The frontend SHALL provide a `v-permission` directive that hides or disables UI elements based on the user's permissions array from auth store.

#### Scenario: Button hidden without permission

- **WHEN** a button has `v-permission="'inspection:void'"` and the current user's permissions do not include `inspection:void` or `*`
- **THEN** the button SHALL NOT be rendered in the DOM

#### Scenario: Button visible with wildcard permission

- **WHEN** a button has `v-permission="'inspection:void'"` and the current user's permissions include `*`
- **THEN** the button SHALL be rendered and clickable

### Requirement: Admin role and permission management API

The system SHALL provide admin endpoints for role CRUD at `/api/v1/admin/roles` and permission assignment at `/api/v1/admin/roles/{id}/menus` and `/api/v1/admin/roles/{id}/permissions`.

#### Scenario: Admin assigns menus to role

- **WHEN** an ADMIN submits menu ids [1, 2, 5] to assign to role QUALITY_ENGINEER
- **THEN** the system SHALL replace all sys_role_menu entries for that role with the specified menu ids and invalidate cached menu trees for affected users

#### Scenario: Admin assigns permissions to role

- **WHEN** an ADMIN submits permission codes to assign to a role
- **THEN** the system SHALL update sys_role_permission accordingly

### Requirement: Route guard checks menu permission

The frontend router guard SHALL verify that the target route's associated perm_code is present in the user's permissions before allowing navigation.

#### Scenario: Unauthorized route access redirected

- **WHEN** a user navigates to a route requiring permission `admin:user:manage` but lacks that permission
- **THEN** the router SHALL redirect to `/dashboard` and display a permission denied message

### Requirement: Backward compatibility with single role field

The system SHALL retain `sys_user.role` as a compatibility field during migration, automatically syncing the primary role to sys_user_role on user create/update.

#### Scenario: Legacy role field synced on user update

- **WHEN** an ADMIN updates a user's role via the existing user management API setting role to QUALITY_MANAGER
- **THEN** the system SHALL update both sys_user.role and ensure sys_user_role contains the corresponding association

### Requirement: Permission codes for existing business operations

The system SHALL define permission codes covering existing role-gated operations: standard management (QUALITY_ENGINEER), inspection void (QUALITY_SUPERVISOR), rejudgment approval (QUALITY_MANAGER), concession approval (SALES_MANAGER, QUALITY_MANAGER), and admin operations (ADMIN).

#### Scenario: Seed script creates business permission codes

- **WHEN** the RBAC seed script is executed
- **THEN** permission codes SHALL be created for all operations currently guarded by `@SaCheckRole` or `AuthUtils.hasRole` in Service layer, with default role-permission mappings matching current behavior
