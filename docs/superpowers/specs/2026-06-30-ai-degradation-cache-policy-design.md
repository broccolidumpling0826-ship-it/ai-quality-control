# AI 降级缓存策略调整设计

## 背景

当前系统已有 `qc_ai_cache` 表和 `AiDegradationService`，但缓存主要来自初始化或迁移脚本预置，运行时没有“模型回答通过校验后自动写入缓存”的路径。部分场景使用缓存的顺序也不一致：质保书问答当前会先查缓存再调用模型，判定解释和标准 RAG 则基本不使用 `qc_ai_cache` 作为失败兜底。

本次调整目标是统一 AI 降级缓存策略：缓存只作为模型不可用、模型失败或模型输出未通过校验时的兜底，不抢占正常模型调用路径；模型输出通过可信校验后，再写入缓存，供后续同输入快照的降级场景使用。

## 目标

- 在 `application.yml` 中增加 AI 降级缓存配置，控制解释/问答类 AI 场景是否读写缓存。
- 缓存读取采用 `FALLBACK_ONLY` 语义：模型正常且输出通过校验时使用 `GENERATED`，不使用 `CACHE`。
- 模型输出通过当前场景的可信校验后，自动写入 `qc_ai_cache`。
- 缓存写入采用“新增版本并停用旧版本”的策略，保留历史记录但运行时只命中最新有效缓存。
- 缓存命中必须校验 `inputHash`，避免标准、证据、Prompt 或模型变化后误用旧解释。
- 覆盖判定解释、质保书问答、标准 RAG、让步风险说明等解释/问答类 AI 输出。

## 非目标

- 不把所有 AI 场景重构成一个统一编排器。
- 不改变结构化规则判定的权威地位。
- 不缓存未经引用或事实校验的原始模型响应。
- 不把 `qc_ai_cache` 替代 `qc_ai_assessment` 审计表。

## 配置设计

在现有 `app.ai` 命名空间下新增配置：

```yaml
app:
  ai:
    degradation:
      cache:
        enabled: true
        mode: FALLBACK_ONLY
        ttl-days: 30
        write-on-validated-generated: true
```

字段语义：

- `enabled`：总开关。为 `false` 时，不读、不写 `qc_ai_cache`。
- `mode`：缓存使用模式。本次实现 `FALLBACK_ONLY`，表示缓存只在模型不可用、模型失败或输出校验失败后兜底。
- `ttl-days`：自动写入缓存的有效天数，默认建议 30 天。
- `write-on-validated-generated`：模型输出通过校验后是否写入缓存，默认 `true`。

运行时语义：

```text
模型正常且输出通过校验
  -> 返回 GENERATED
  -> 写入 qc_ai_cache 新版本

模型不可用 / 调用失败 / 输出校验失败
  -> 若缓存开关开启，则按当前 inputHash 查找有效缓存
  -> 命中则返回 CACHE
  -> 未命中则继续 RULE_TEMPLATE / RAW_RETRIEVAL / UNAVAILABLE
```

## 缓存键与输入哈希

缓存命中条件从当前的：

```text
assessmentType + businessType + businessId + enabled + expiryTime
```

升级为：

```text
assessmentType + businessType + businessId + promptVersion + inputHash + enabled + expiryTime
```

`inputHash` 应由稳定输入快照计算，不应只包含用户问题，也不应包含当前时间、traceId 等易抖动字段。

各场景建议纳入 `inputHash` 的字段：

- 判定解释：判定类型、判定证据、命中标准 ID、引用条款 ID/条款号/段落摘要、Prompt 版本、模型名。
- 质保书问答：问题、卷号/批次号、质保书状态、最终判定、指标依据、引用条款、Prompt 版本、模型名。
- 标准 RAG：用户问题、过滤条件、用于回答的来源条款 ID 和段落摘要、Prompt 版本、模型名。
- 让步风险：判定 ID、风险维度输入、偏差证据、客户用途/画像、历史案例引用、替代库存摘要、Prompt 版本、模型名。

如果无法构造 `inputHash`，禁止读写缓存，避免误命中。

## 服务边界

推荐新增或扩展一个统一的缓存策略服务，例如 `AiFallbackCacheService`。该服务不负责业务 prompt、模型调用或引用校验，只负责缓存策略。

核心职责：

```text
buildInputHash(...)
findFallbackCache(...)
saveValidatedGenerated(...)
```

建议行为：

- `findFallbackCache` 只在 `enabled=true` 且 `mode=FALLBACK_ONLY` 且存在 `inputHash` 时查询缓存。
- 查询时要求 `assessmentType`、`businessType`、`businessId`、`promptVersion`、`inputHash` 一致，且 `enabled=1`、未过期。
- `saveValidatedGenerated` 只保存已经通过校验的最终展示文本。
- 写入前停用同 `assessmentType + businessType + businessId + promptVersion + inputHash` 下旧的启用缓存，再插入新缓存。
- 缓存写入失败不影响本次 `GENERATED` 返回。
- 缓存查询失败不影响主流程，继续规则模板或原始检索降级。

## 场景接入

### 判定解释

流程调整为：

```text
构造规则解释和引用
  -> 调用模型
  -> 模型输出通过 CitationReferenceSupport 校验
       -> 返回 GENERATED
       -> 写入缓存
  -> 模型失败或校验失败
       -> 按 inputHash 查缓存
       -> 命中返回 CACHE
       -> 未命中则返回结构化规则解释、规则模板或原始检索
```

当前判定解释没有运行时查 `qc_ai_cache`，本次调整后会增加失败兜底缓存读取。

### 质保书问答

当前质保书问答是先查缓存再调模型。调整为：

```text
优先调用模型
  -> 输出通过校验则返回 GENERATED 并写缓存
  -> 模型失败或校验失败后再查缓存
  -> 缓存未命中再返回规则模板或原始检索
```

这样可以避免正常情况下旧缓存覆盖新的可信模型回答。

### 标准 RAG

标准 RAG 保持先检索、再调模型的主路径：

```text
检索来源条款
  -> 来源不足则拒答或 RAW_RETRIEVAL
  -> 来源可用则调用模型
  -> 模型输出通过校验则 GENERATED 并写缓存
  -> 模型失败或校验失败则先查缓存
  -> 缓存未命中再 RAW_RETRIEVAL
```

提示注入场景仍应优先触发安全保护，不调用模型。是否允许查缓存需要由当前输入哈希控制；如果输入包含提示注入文本导致 hash 不一致，旧缓存不会命中。

### 让步风险说明

让步风险当前模型主要用于优化话术，引用校验弱于判定解释。为了避免缓存不可信文字，本次应补齐最低限度校验：

```text
先生成规则风险结果和规则说明
  -> 模型输出通过可信校验后，才返回 GENERATED 并写缓存
  -> 模型失败或校验失败后可查同 inputHash 缓存
  -> 缓存未命中则保留 RULE_TEMPLATE
```

如果无法补齐有效校验，则该场景不应自动写入缓存。

## 审计设计

两张表分工保持清晰：

- `qc_ai_assessment`：记录每次 AI 辅助评估或展示结果。
- `qc_ai_cache`：记录可用于降级兜底的可信缓存版本。

模型通过校验并写缓存时：

- 返回结果标记 `degradationSource=GENERATED`。
- `qc_ai_assessment.cache_hit=0`。
- `qc_ai_cache.degradation_source=CACHE`。
- `qc_ai_cache.cached_output` 保存最终可信展示内容和必要结构化字段。
- 日志记录 `cacheKey`、`assessmentType`、`businessId`、`inputHash`、`promptVersion`。

模型失败但缓存命中时：

- 返回结果标记 `degradationSource=CACHE`。
- `qc_ai_assessment.cache_hit=1`。
- `confidenceLabel` 使用缓存自身保存的值。
- `confidenceFactors` 增加“模型输出不可用，命中同输入快照缓存”之类的因素说明。

## 错误处理

- 缓存查询失败：记录 warn，继续规则模板、原始检索或不可用降级。
- 缓存写入失败：记录 warn，本次仍返回 `GENERATED`。
- 配置关闭：不读、不写缓存，其他降级逻辑保持现状。
- `inputHash` 缺失：禁止读写缓存。
- 缓存过期：不命中。
- 缓存内容需要通过当前场景最低可信展示要求；不能把原始模型响应直接展示为缓存结果。

## 测试策略

- 配置绑定测试：验证默认值、关闭开关、TTL。
- 缓存服务测试：有效缓存命中、过期缓存跳过、`inputHash` 不一致不命中。
- 写入测试：通过校验后插入新缓存，并停用同 key/hash 的旧启用缓存。
- 判定解释测试：模型通过校验返回 `GENERATED` 并写缓存；模型失败后命中 `CACHE`；缓存关闭后走规则模板。
- 质保书问答测试：不再前置 cache；模型失败才 cache fallback。
- 标准 RAG 测试：模型输出校验失败时优先查缓存，未命中再 `RAW_RETRIEVAL`。
- 让步风险测试：未经校验的模型话术不写缓存；模型失败时可用同输入缓存兜底。

## 迁移与兼容

现有 P0 seed 缓存可保留，但由于新增 `inputHash` 命中要求，旧缓存如果没有可匹配的 `inputHash`，默认不会被运行时自动命中。可以选择后续补一份迁移脚本，为仍需保留的演示缓存补齐稳定 `inputHash`。

如果希望 P0 演示继续使用旧缓存，可以通过单独 seed 或迁移脚本生成与当前输入快照一致的 `inputHash`，不建议在代码里放宽 `inputHash` 要求。

## 决策记录

- 缓存策略采用 `FALLBACK_ONLY`，不采用 `CACHE_FIRST`。
- 缓存写入采用新增版本并停用旧版本。
- 缓存有效性采用 `inputHash + TTL` 双重控制。
- 缓存兜底适用于模型不可用、模型调用失败、模型输出校验失败。
- 本次采用统一缓存策略服务，不做统一 AI 输出编排器重构。
