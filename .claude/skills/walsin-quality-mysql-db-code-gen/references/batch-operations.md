---
description: Service批量操作方法开发规范
globs: **/*Service.java
alwaysApply: false
---

# Service 批量操作方法开发规范

## 标准结构要求

对于一般的业务实体对应的 Service 类，必须包含以下标准结构：

### 1. 字段常量定义

在 Service 类中必须定义以下两个常量（集合中的元素由开发者根据业务需求指定）：

```java
private static final List<SFunction<EntityName, ?>> NOT_BLANK_FIELDS = ListUtil.of(
    EntityName::getField1,
    EntityName::getField2,
    EntityName::getField3
);

private static final List<SFunction<EntityName, ?>> UNIQUE_FIELDS = ListUtil.of(
    EntityName::getField1,
    EntityName::getField2,
    EntityName::getField3
);
```

**要求：**
- 使用 `ListUtil.of()` 创建不可变列表
- 使用 `SFunction<EntityName, ?>` 类型，通过方法引用指定字段
- `NOT_BLANK_FIELDS` 用于非空校验
- `UNIQUE_FIELDS` 用于唯一性校验
- 集合中的元素由开发者根据业务需求手动指定

### 2. 批量操作方法

必须实现以下三个批量操作方法：

#### saveBatch 方法
```java
public void saveBatch(List<EntityName> entities) {
    this.validateSaveBatch(entities);
    super.saveBatch(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
}
```

#### updateBatch 方法
```java
public void updateBatch(List<EntityName> entities) {
    this.validateUpdateBatch(entities);
    super.updateBatchById(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
}
```

#### saveOrUpdateBatch 方法
```java
public void saveOrUpdateBatch(List<EntityName> entities) {
    BusinessUtil.saveOrUpdateBatch(
        entities,
        UNIQUE_FIELDS,
        super.baseMapper,
        this::saveBatch,
        this::updateBatch
    );
}
```

### 3. 校验方法

必须实现以下私有校验方法：

#### validateSaveBatch 方法
```java
private void validateSaveBatch(List<EntityName> entities) {
    this.validateNotBlankBatch(entities);
    this.validateUniqueBatch(entities);
}
```

#### validateUpdateBatch 方法
```java
private void validateUpdateBatch(List<EntityName> entities) {
    this.validateNotBlankBatch(entities);
    this.validateUniqueBatch(entities);
}
```

#### validateNotBlankBatch 方法
```java
private void validateNotBlankBatch(List<EntityName> entities) {
    BusinessUtil.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
}
```

#### validateUniqueBatch 方法
```java
private void validateUniqueBatch(List<EntityName> entities) {
    BusinessUtil.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
}
```

### 4. 工具方法（可选）

可以添加获取唯一标识的工具方法：

```java
public static String getUniqueTag(EntityName entity) {
    return BusinessUtil.extractUniqueTag(entity, UNIQUE_FIELDS);
}
```

## 完整示例

参考 `MeterAcctDtlService` 的实现：

```java
@DS(PrfMicroServiceName.PERFORMANCE)
@Log4j2
@Service
@RequiredArgsConstructor
public class MeterAcctDtlService extends JhServiceImpl<MeterAcctDtlMapper, MeterAcctDtl> {

    private static final List<SFunction<MeterAcctDtl, ?>> NOT_BLANK_FIELDS = ListUtil.of(
        MeterAcctDtl::getEmployeeNo,
        MeterAcctDtl::getMeterStdId,
        MeterAcctDtl::getAcctDate
    );

    private static final List<SFunction<MeterAcctDtl, ?>> UNIQUE_FIELDS = ListUtil.of(
        MeterAcctDtl::getEmployeeNo,
        MeterAcctDtl::getMeterStdId,
        MeterAcctDtl::getAcctDate
    );

    // ... 其他依赖注入

    public void saveOrUpdateBatch(List<MeterAcctDtl> entities) {
        BusinessUtil.saveOrUpdateBatch(
            entities,
            UNIQUE_FIELDS,
            super.baseMapper,
            this::saveBatch,
            this::updateBatch
        );
    }

    public void saveBatch(List<MeterAcctDtl> entities) {
        this.validateSaveBatch(entities);
        super.saveBatch(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void updateBatch(List<MeterAcctDtl> entities) {
        this.validateUpdateBatch(entities);
        super.updateBatchById(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    private void validateSaveBatch(List<MeterAcctDtl> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateUpdateBatch(List<MeterAcctDtl> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateNotBlankBatch(List<MeterAcctDtl> entities) {
        BusinessUtil.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
    }

    private void validateUniqueBatch(List<MeterAcctDtl> entities) {
        BusinessUtil.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
    }

    public static String getUniqueTag(MeterAcctDtl entity) {
        return BusinessUtil.extractUniqueTag(entity, UNIQUE_FIELDS);
    }
}
```

## 注意事项

1. **字段选择**：`NOT_BLANK_FIELDS` 和 `UNIQUE_FIELDS` 中的字段由开发者根据业务需求手动指定，AI 不应自动填充
2. **批量大小**：使用 `BusConstant.DEFAULT_DB_BATCH_SIZE` 作为批量操作的大小
3. **校验顺序**：先进行非空校验，再进行唯一性校验
4. **方法可见性**：校验方法使用 `private` 修饰，批量操作方法使用 `public` 修饰
5. **依赖要求**：确保导入了 `cn.hutool.core.collection.ListUtil` 和 `com.baomidou.mybatisplus.core.toolkit.support.SFunction`

## 创建新 Service 时的要求

当创建新的业务实体对应的 Service 类时，必须：
1. 自动生成上述所有方法的结构框架
2. 在 `NOT_BLANK_FIELDS` 和 `UNIQUE_FIELDS` 中留空，等待开发者根据业务需求填充
3. 确保所有方法签名和实现逻辑与标准模板一致
