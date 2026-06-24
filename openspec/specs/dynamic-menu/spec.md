# dynamic-menu

## Purpose

Configurable navigation and routing driven by database-backed menu trees, replacing hardcoded sidebar and static router children.

## Requirements

### Requirement: Menu data stored in database

The system SHALL persist menu configuration in the `sys_menu` table with tree structure (parent_id), supporting fields: menu_name, menu_type, path, component, icon, perm_code, visible, sort_order, status, and meta_json.

#### Scenario: Admin creates a new menu item

- **WHEN** an ADMIN user submits a valid menu creation request with menu_name, menu_type, path, and parent_id
- **THEN** the system SHALL persist the menu record and return the created menu with generated id

#### Scenario: Menu tree query returns hierarchical structure

- **WHEN** a logged-in user requests `GET /api/v1/menus/user-tree`
- **THEN** the system SHALL return a nested tree of menus filtered by the user's roles, sorted by sort_order

### Requirement: Menu types support multiple navigation modes

The system SHALL support menu_type values: DIR (directory/group), MENU (internal Vue page), HIDDEN (registered route not shown in sidebar), LINK (external URL), IFRAME (embedded page).

#### Scenario: DIR menu appears as sidebar group

- **WHEN** a menu item has menu_type DIR
- **THEN** the sidebar SHALL render it as a collapsible group header with child items nested beneath

#### Scenario: HIDDEN menu registers route but not sidebar

- **WHEN** a menu item has menu_type HIDDEN and visible=0
- **THEN** the frontend SHALL register the route for direct navigation but SHALL NOT display it in the sidebar

#### Scenario: LINK menu opens external URL

- **WHEN** a menu item has menu_type LINK and meta_json contains externalUrl
- **THEN** clicking the menu item SHALL open the URL in a new browser tab

### Requirement: Frontend dynamic route registration

The frontend SHALL register business routes dynamically after login by calling `router.addRoute()` based on the menu tree returned from the API, using `import.meta.glob` to resolve Vue components.

#### Scenario: Routes registered after successful login

- **WHEN** a user logs in successfully
- **THEN** the frontend SHALL fetch the user menu tree, build RouteRecordRaw entries, and add them to the router before navigating to the redirect target

#### Scenario: Routes restored on page refresh

- **WHEN** a logged-in user refreshes the browser
- **THEN** the frontend SHALL fetch the menu tree during app initialization (before mount) and restore all dynamic routes

#### Scenario: Unknown component path rejected

- **WHEN** a menu item references a component path not found in the glob scan result
- **THEN** the frontend SHALL skip route registration for that item and log a console warning

### Requirement: Sidebar rendered from API menu tree

The frontend SHALL render the sidebar navigation exclusively from the menu store tree, replacing all hardcoded nav items in MainLayout.vue.

#### Scenario: Different roles see different menus

- **WHEN** user A (QUALITY_ENGINEER) and user B (ADMIN) log in
- **THEN** user B SHALL see all menus assigned to ADMIN role including admin management section, and user A SHALL see only menus assigned to QUALITY_ENGINEER role

#### Scenario: Disabled menu not shown

- **WHEN** a menu item has status=0 (disabled)
- **THEN** the menu tree API SHALL exclude it from the user-tree response

### Requirement: Admin menu management API

The system SHALL provide CRUD endpoints at `/api/v1/admin/menus` accessible only to users with `admin:menu:manage` permission (or ADMIN role during migration).

#### Scenario: Admin updates menu sort order

- **WHEN** an ADMIN user submits a batch sort update with menu ids and new sort_order values
- **THEN** the system SHALL update all specified menus and invalidate cached menu trees

#### Scenario: Admin deletes menu with children

- **WHEN** an ADMIN user attempts to delete a menu that has child menus
- **THEN** the system SHALL reject the request with error message indicating children must be removed first

### Requirement: Initial menu seed from existing routes

The system SHALL provide a seed script that imports all existing routes from the current router configuration as initial sys_menu records with appropriate role assignments matching current access patterns, and it SHALL include new AI quality routes introduced by this change.

#### Scenario: Seed script populates all business pages

- **WHEN** the seed script is executed on a fresh database
- **THEN** all 19+ existing business routes SHALL be present in sys_menu including hidden form/detail pages as HIDDEN type

#### Scenario: Seed script populates AI quality pages

- **WHEN** the seed script is executed on a fresh database after this change
- **THEN** P0 AI quality pages SHALL be present in sys_menu, including standard RAG retrieval, standard conflict detection, quality certificate Q&A, and hidden conflict裁决/detail routes

#### Scenario: Seed script assigns AI permissions

- **WHEN** the seed script populates AI quality pages
- **THEN** it SHALL assign menu and action permissions for AI retrieval, conflict裁决, certificate Q&A, confidence configuration, and AI assessment review according to the target roles
