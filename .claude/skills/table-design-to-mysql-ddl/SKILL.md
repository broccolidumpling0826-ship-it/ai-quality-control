---
name: table-design-to-mysql-ddl
description: 将表格形式的数据库表设计转换为 MySQL 建表语句（CREATE TABLE DDL）。当用户提供数据库表设计文档（包含字段英文名、中文名、类型、长度、主外键、是否为空、索引、备注等信息），或说"生成建表语句"、"转成SQL"、"帮我写DDL"、"生成MySQL建表"等需求时，必须使用此 skill。输入可以是 Markdown 表格、纯文本描述、或结构化字段列表，输出标准的 MySQL CREATE TABLE 语句。
---

# 表格设计 → MySQL DDL 生成 Skill

## 目标

用户提供数据库表设计（表名、字段列表含类型/长度/约束/备注等），生成规范的 MySQL `CREATE TABLE` 建表语句。

---

## 第一步：解析输入信息

用户输入通常包含以下部分，逐一提取：

### 表级信息
- **表名（英文）**：如 `qmmc_process_tech_param`
- **表中文名 / 注释**：如 `制程工艺参数表`
- **菜单/页面名**（可选，仅作参考，不写入 DDL）

### 字段信息（每行一个字段）
从表格列中提取以下属性：

| 列名 | 说明 |
|---|---|
| 字段英文名 | 列名，保持原始下划线命名 |
| 字段中文名 | 用作 `COMMENT` |
| 主/外键 | "主键"→`PRIMARY KEY`，"外键"→仅加注释，不生成外键约束（见下方规则） |
| 类型 | varchar / int / decimal / datetime / text 等 |
| 长度 | 括号内数值，如 `varchar(40)` |
| 是否为空 | "N" 或 "否" → `NOT NULL`；空白/Y/是 → 允许为空（不写 NOT NULL） |
| 是否索引 | "是" / "Y" → 生成 `INDEX` |
| 缺省 | `DEFAULT` 值，空白则不写 |
| 备注 | 追加到 `COMMENT` 后面（用"；"与中文名分隔） |

---

## 第二步：类型映射规则

| 输入类型 | MySQL 类型 | 说明 |
|---|---|---|
| varchar + 长度 | `VARCHAR(n)` | 直接对应 |
| varchar（无长度） | `VARCHAR(255)` | 默认 255 |
| char + 长度 | `CHAR(n)` | |
| int / integer | `INT` | |
| int + 长度 | `INT(n)` | |
| decimal(m,n) | `DECIMAL(m,n)` | |
| number | `DECIMAL(18,6)` | 通用数值默认 |
| datetime / timestamp | `DATETIME` | |
| date | `DATE` | |
| text / clob | `TEXT` | |
| bigint | `BIGINT` | |

---

## 第三步：约束与索引规则

### 主键
- 字段主/外键列标注"主键"的字段 → 在列定义末尾标记，同时在末尾用 `PRIMARY KEY (字段名)` 声明
- 主键字段必须 `NOT NULL`

### 外键
- 标注"外键"的字段：**不生成 FOREIGN KEY 约束**，只在 `COMMENT` 中补充"外键-关联xxx"
- 原因：实际项目中常避免数据库层外键约束，用应用层维护

### 索引
- 字段"是否索引"为"是"/"Y"时，在 `CREATE TABLE` 末尾添加 `INDEX idx_表名_字段名 (字段名)`
- 主键自带索引，无需重复

### NOT NULL
- 只有明确标注 "N" 或 "否" 时才写 `NOT NULL`，其余留空

---

## 第四步：COMMENT 规则

每个字段都加 `COMMENT`，内容拼接规则：

```
字段中文名[；备注内容（如有）]
```

示例：
- 只有中文名：`COMMENT '管控类型'`
- 有备注：`COMMENT '管控类型；字典（制程管控/现场管控）'`
- 外键字段：`COMMENT '所属工作站id；外键-关联工作站'`

表本身加 `COMMENT = '表中文名'`。

---

## 第五步：标准输出格式

```sql
create table 表名 (
  字段1 varchar(20) comment '字段1中文名',
  字段2 varchar(40) not null comment '字段2中文名；外键-关联xxx',
  id varchar(40) not null comment 'ID主键',
  create_user_no varchar(20) not null comment '创建人',
  create_date_time varchar(40) not null comment '创建时间',
  update_user_no varchar(20) comment '更新人',
  update_date_time varchar(40) comment '更新时间',
  company_id varchar(40) not null comment '公司别',
  primary key (id),
  index idx_表名_字段名 (字段名)
) comment '表中文名' collate = utf8mb4_general_ci;
```

### 格式要求
- **所有关键字（CREATE TABLE、VARCHAR、NOT NULL、PRIMARY KEY、INDEX、ENGINE 等）全部小写**
- 表名和列名**不加任何引号或反引号**，直接裸写
- 列定义每行缩进 2 个空格
- 约束声明（PRIMARY KEY、INDEX）放在所有列定义之后，用逗号分隔
- 最后一行约束后无逗号
- 固定结尾：`omment '...' collate = utf8mb4_general_ci`
- 字段顺序：按用户提供的顺序输出（不做重排），但 PRIMARY KEY 和 INDEX 始终放末尾

---

## 第六步：输出完整 SQL

直接输出 SQL 代码块，无需额外解释（除非用户问题本身有歧义需要确认）。

如果用户提供了多张表，逐一生成，每张表之间空一行分隔。

---

## 边界情况处理

| 情况 | 处理方式 |
|---|---|
| 长度列为空 | varchar 默认 255，其他类型按上方映射表处理 |
| 备注中有换行/多行说明 | 合并为一行，写入 COMMENT |
| 字段名含大写 | 转为小写下划线（保持原始格式，不强制转换） |
| 没有主键字段 | 不生成 PRIMARY KEY 行，但建议在输出后提示用户 |
| 同一字段既是主键又标注索引 | 只生成 PRIMARY KEY，不重复生成 INDEX |
