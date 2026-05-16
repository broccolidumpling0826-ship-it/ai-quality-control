---
name: java-controller-to-json
description: Parse Java Spring Boot Controller files and extract interface information (purpose, method, path, input/output param structures) into structured JSON files for frontend code generation. Use this skill whenever the user wants to analyze backend Controller interfaces, extract API information from Java files, generate interface structure JSON, document REST API endpoints, or prepare backend interface data to assist in generating frontend pages. Trigger when the user mentions "解析接口"、"提取Controller接口信息"、"生成接口json"、"controller转json"、"接口结构文件"、"前端接口信息"、"IStruct.json" or any request involving reading Java Controller files and outputting structured API data.
---

# Java Controller to JSON

This skill reads one or more Java Spring Boot Controller files, parses all REST API endpoint definitions, resolves DTO/Entity class structures, and writes structured JSON files for use by frontend code generation agents.

## Required Inputs (enforce strictly)

Before doing anything else, verify the user has provided **both** of the following:

1. **One or more Controller `.java` file paths** (absolute paths)
2. **An output directory path** where the JSON files should be written

If either is missing, **stop immediately** and tell the user exactly what is needed. Do not proceed with parsing.

Example prompt to user when inputs are incomplete:
> 需要以下两项信息才能继续：
> 1. Controller .java 文件的完整路径（可以提供多个）
> 2. 生成的 JSON 文件的输出目录路径
> 请补充后再继续。

---

## Step 1: Read and Parse Each Controller File

Use the `Read` tool to read each Controller `.java` file. Extract the following:

### Class-level
- `@RequestMapping("/xxx")` → `baseUrl` (URL prefix for all methods in this controller)
- `@Api(value = "...")` → `controllerDescription`
- Class name → `controllerName`

### Method-level (for each public method)
- `@ApiOperation(value = "...")` → `purpose`
- HTTP Mapping annotation → `method` (uppercase):
  - `@PostMapping` → `POST`
  - `@GetMapping` → `GET`
  - `@PutMapping` → `PUT`
  - `@DeleteMapping` → `DELETE`
  - `@PatchMapping` → `PATCH`
  - `@RequestMapping(method = RequestMethod.XXX)` → parse accordingly
- Path in the Mapping annotation + `baseUrl` → `path` (full URL)
- Method parameters with their annotations → determines `inputParam`
- Return type with generics (e.g., `ApiResult<MeterAcctActivity>`) → determines `outputParam`

---

## Step 2: Infer Project Source Root

From the Controller file's absolute path, walk upward to find the `src/main/java` directory. This becomes `javaSourceRoot`.

Example:
- Controller: `/project/src/main/java/com/example/controller/FooController.java`
- `javaSourceRoot`: `/project/src/main/java`

---

## Step 3: Resolve Entity/DTO Class Fields (recursive, max depth 3)

For each entity class referenced in method parameters or return types:

1. Find its fully-qualified class name from the `import` statements in the Controller file (e.g., `com.example.dto.MeterAcctActivityDTO`)
2. Convert to file path: replace `.` with `/`, append `.java`, prepend `javaSourceRoot`
3. Read the class file with the `Read` tool
4. Extract all fields:
   - Field name → JSON key
   - Java type → `type`
   - `@ApiModelProperty(value = "...")` → `description`
5. If a field's type is another entity class (not a primitive, not String, not a Java built-in), recurse into it — up to 3 levels deep. Track visited class names to avoid circular references.

If the import is not found in the Controller file, use `Glob` or `Grep` to search the project for the class file by name.

### Special case: CoreEntity (known external base class)

When a class extends `CoreEntity` and the `CoreEntity.java` source file cannot be found, use the following **fixed field definitions** (do not guess or invent other fields):

```json
{
  "id":             { "type": "String",  "description": "id,新增时不填,修改时必填" },
  "companyId":      { "type": "String",  "description": "公司ID", "hidden": true },
  "createUserNo":   { "type": "String",  "description": "创建人工号:strAccount", "hidden": true },
  "updateUserNo":   { "type": "String",  "description": "修改人工号:strAccount", "hidden": true },
  "createDateTime": { "type": "String",  "description": "创建日期", "hidden": true },
  "updateDateTime": { "type": "String",  "description": "修改日期", "hidden": true }
}
```

Fields marked `"hidden": true` are framework-managed and normally not exposed to the frontend, but should still be included in the JSON for completeness.

---

## Step 4: Build inputParam Structure

### Handle @ApiIgnore parameters (pagination params)

Some methods have a pagination parameter annotated with `@ApiIgnore` (e.g., `@ApiIgnore JhPage page`). This means the parameter is **injected by the framework**, not directly passed by the frontend caller. Treat it as a special `pagination` entry:

- Do NOT flatten its fields into the main query fields
- Record it separately as `"pagination"` alongside `"queryFields"`
- Infer standard pagination fields from JhPage convention: `pageNum` (current page, default 1) and `pageSize` (page size, default 10)

**For methods with both `@ApiIgnore JhPage` and a query object (the typical list pattern):**
```json
{
  "paramStyle": "query",
  "pagination": {
    "description": "分页参数（由框架注入，前端传 pageNum/pageSize）",
    "fields": {
      "pageNum": { "type": "Integer", "description": "当前页码，默认1" },
      "pageSize": { "type": "Integer", "description": "每页大小，默认10" }
    }
  },
  "queryFields": {
    "fieldName": { "type": "String", "description": "查询条件字段含义" }
  }
}
```

### Determine paramStyle from other annotations

| Annotation | `paramStyle` |
|---|---|
| `@RequestBody` | `"body"` |
| `@RequestParam` | `"query"` |
| `@PathVariable` | `"path"` |
| No annotation (simple type) | `"query"` |
| `@ApiIgnore` | Handled as pagination above, not as a regular param |

**For `@RequestBody` (object input):**
```json
{
  "paramStyle": "body",
  "type": "object",
  "fields": {
    "fieldName": {
      "type": "String",
      "description": "字段含义"
    }
  }
}
```

**For `@RequestParam` / `@PathVariable` (flat params):**
```json
{
  "paramStyle": "query",
  "type": "object",
  "fields": {
    "paramName": {
      "type": "String",
      "description": "参数说明"
    }
  }
}
```

**For no parameters:**
```json
{
  "paramStyle": "none",
  "type": "null"
}
```

---

## Step 5: Build outputParam Structure

All controller methods return `ApiResult<T>`. Expand the full structure as follows:

| Return Type | `data` field structure |
|---|---|
| `ApiResult<T>` | Expanded T object fields |
| `ApiResult<List<T>>` | Array of expanded T object |
| `ApiResult<IPage<T>>` | `{ records: [T fields], total, size, current, pages }` |
| `ApiResult<Void>` or bare `ApiResult` | `null` |
| `ApiResult<String>` / `ApiResult<Boolean>` etc. | The primitive type |

Always include the outer wrapper:
```json
{
  "type": "object",
  "fields": {
    "code": { "type": "int", "description": "状态码，200为成功" },
    "message": { "type": "String", "description": "提示信息" },
    "data": { ... }
  }
}
```

For `IPage<T>`, the `data` looks like:
```json
{
  "type": "object",
  "fields": {
    "records": {
      "type": "array",
      "itemType": "object",
      "fields": { ... }
    },
    "total": { "type": "long", "description": "总记录数" },
    "size": { "type": "long", "description": "每页大小" },
    "current": { "type": "long", "description": "当前页码" },
    "pages": { "type": "long", "description": "总页数" }
  }
}
```

---

## Step 6: Assemble Final JSON Structure

Build a JSON object per Controller:

```json
{
  "controllerName": "MeterAcctActivityController",
  "controllerDescription": "计量核算活动",
  "baseUrl": "/meterAcctActivity",
  "interfaces": [
    {
      "purpose": "新增计量核算活动",
      "method": "POST",
      "path": "/meterAcctActivity/save",
      "inputParam": {
        "paramStyle": "body",
        "type": "object",
        "fields": {
          "name": {
            "type": "String",
            "description": "活动名称"
          },
          "startDate": {
            "type": "Date",
            "description": "开始日期"
          }
        }
      },
      "outputParam": {
        "type": "object",
        "fields": {
          "code": { "type": "int", "description": "状态码，200为成功" },
          "message": { "type": "String", "description": "提示信息" },
          "data": {
            "type": "object",
            "fields": {
              "id": { "type": "Long", "description": "主键ID" },
              "name": { "type": "String", "description": "活动名称" }
            }
          }
        }
      }
    }
  ]
}
```

---

## Step 7: Write JSON Files

- **File naming**: Strip `Controller` suffix from class name, append `IStruct.json`
  - `MeterAcctActivityController` → `MeterAcctActivityIStruct.json`
- **Write location**: The user-specified output directory
- Use the `Write` tool to write each file
- After all files are written, output a summary listing each file's full path

Example summary:
```
已生成以下接口结构文件：
- /path/to/output/MeterAcctActivityIStruct.json  (3 个接口)
- /path/to/output/MeterOrderController.json       (5 个接口)
```

---

## Edge Cases to Handle

- **Multiple path values in mapping**: `@GetMapping({"/list", "/all"})` → use the first path
- **No `@ApiOperation`**: Use method name as `purpose` fallback
- **No `@Api` on class**: Leave `controllerDescription` as empty string
- **Enum field types**: Note as `type: "String (enum)"` and list enum values if readable from the file
- **Nested generics**: `List<Map<String, Object>>` → represent as `type: "array", itemType: "Map"`
- **Import not found**: If a class cannot be located, record `{ "type": "UnresolvedType", "description": "未能解析，请手动补充" }` and log a warning to the user after completion
