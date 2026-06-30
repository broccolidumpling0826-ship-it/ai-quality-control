# AI 降级缓存策略 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 `FALLBACK_ONLY` AI 降级缓存策略：模型输出通过校验时返回 `GENERATED` 并写入缓存，模型失败或校验失败时才按同输入快照读取 `CACHE` 兜底。

**Architecture:** 新增一个独立的 AI 缓存策略服务，集中处理配置、`inputHash`、缓存查找、版本化写入和旧版本停用。各业务服务仍保留自己的 prompt、模型调用和可信校验逻辑，只在生成成功后调用写缓存，在失败分支调用缓存兜底。缓存使用 `assessmentType + businessType + businessId + promptVersion + inputHash + enabled + expiryTime` 命中，避免旧证据误用。

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis-Plus, JUnit 5, Mockito, MySQL 5.7.

---

## 文件结构

- 新建：`backend/src/main/java/com/jhict/quality/config/AiDegradationCacheProperties.java`
  - 绑定 `app.ai.degradation.cache` 配置。
- 修改：`backend/src/main/resources/application.yml`
  - 增加默认配置与环境变量默认值。
- 新建：`backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheContext.java`
  - 缓存读写上下文，包含业务标识、Prompt 版本、模型名、输入快照、输出、引用、置信度。
- 新建：`backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheService.java`
  - 缓存策略服务接口。
- 新建：`backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImpl.java`
  - 统一实现 `inputHash`、兜底读取、写入新版本并停用旧版本。
- 修改：`backend/src/main/java/com/jhict/quality/service/api/AiCacheService.java`
  - 增加按 `promptVersion + inputHash` 查找、保存可信缓存的方法。
- 修改：`backend/src/main/java/com/jhict/quality/service/impl/AiCacheServiceImpl.java`
  - 实现新版缓存查询和版本化写入。
- 修改：`backend/src/main/java/com/jhict/quality/service/impl/JudgmentServiceImpl.java`
  - 判定解释接入缓存写入和失败兜底。
- 修改：`backend/src/main/java/com/jhict/quality/service/impl/CertQaServiceImpl.java`
  - 将缓存从前置命中改成失败兜底，模型成功后写缓存。
- 修改：`backend/src/main/java/com/jhict/quality/service/impl/StandardRagServiceImpl.java`
  - 模型失败或校验失败时先查缓存，再原始检索。
- 修改：`backend/src/main/java/com/jhict/quality/service/impl/ConcessionRiskServiceImpl.java`
  - 只缓存通过最低可信校验的让步风险说明；失败时缓存兜底。
- 新建：`backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java`
  - 覆盖 hash、开关、过期、版本化写入。
- 修改或新增相关业务服务测试：
  - `backend/src/test/java/com/jhict/quality/service/impl/StandardRagServiceImplTest.java`
  - `backend/src/test/java/com/jhict/quality/service/impl/CertQaServiceImplTest.java`
  - `backend/src/test/java/com/jhict/quality/service/impl/JudgmentServiceImplTest.java`
  - `backend/src/test/java/com/jhict/quality/service/impl/ConcessionRiskServiceImplTest.java`

---

### Task 1: 配置绑定与默认配置

**Files:**
- Create: `backend/src/main/java/com/jhict/quality/config/AiDegradationCacheProperties.java`
- Modify: `backend/src/main/resources/application.yml`
- Test: `backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java`

- [ ] **Step 1: 写配置类**

```java
package com.jhict.quality.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.degradation.cache")
public class AiDegradationCacheProperties {

    private boolean enabled = true;

    private String mode = "FALLBACK_ONLY";

    private Integer ttlDays = 30;

    private boolean writeOnValidatedGenerated = true;

    public boolean isFallbackOnly() {
        return "FALLBACK_ONLY".equalsIgnoreCase(mode);
    }

    public int effectiveTtlDays() {
        return ttlDays == null || ttlDays <= 0 ? 30 : ttlDays;
    }
}
```

- [ ] **Step 2: 增加 `application.yml` 配置**

在 `app.ai` 下加入：

```yaml
    degradation:
      cache:
        enabled: ${AI_DEGRADATION_CACHE_ENABLED:true}
        mode: ${AI_DEGRADATION_CACHE_MODE:FALLBACK_ONLY}
        ttl-days: ${AI_DEGRADATION_CACHE_TTL_DAYS:30}
        write-on-validated-generated: ${AI_DEGRADATION_CACHE_WRITE_ON_VALIDATED_GENERATED:true}
```

- [ ] **Step 3: 写配置默认值测试**

在 `AiFallbackCacheServiceImplTest` 中先写一个最小测试，后续任务会补全该测试类：

```java
package com.jhict.quality.service.support.ai;

import com.jhict.quality.config.AiDegradationCacheProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiFallbackCacheServiceImplTest {

    @Test
    void propertiesDefaultToFallbackOnlyWithThirtyDayTtl() {
        AiDegradationCacheProperties properties = new AiDegradationCacheProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isFallbackOnly()).isTrue();
        assertThat(properties.effectiveTtlDays()).isEqualTo(30);
        assertThat(properties.isWriteOnValidatedGenerated()).isTrue();
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run:

```bash
cd backend && mvn -Dtest=AiFallbackCacheServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/config/AiDegradationCacheProperties.java backend/src/main/resources/application.yml backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java
git commit -m "feat: 添加AI降级缓存配置"
```

---

### Task 2: 缓存上下文与输入哈希

**Files:**
- Create: `backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheContext.java`
- Create: `backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheService.java`
- Create: `backend/src/main/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImpl.java`
- Modify: `backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java`

- [ ] **Step 1: 写缓存上下文对象**

```java
package com.jhict.quality.service.support.ai;

import com.jhict.quality.vo.AiSourceReferenceVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiFallbackCacheContext {

    private String assessmentType;
    private String businessType;
    private String businessId;
    private String promptVersion;
    private String modelName;
    private Map<String, Object> inputSnapshot;
    private String outputText;
    private String structuredOutput;
    private List<AiSourceReferenceVO> references;
    private String confidenceLabel;
    private BigDecimal confidenceScore;
}
```

- [ ] **Step 2: 写服务接口**

```java
package com.jhict.quality.service.support.ai;

import com.jhict.quality.entity.QcAiCache;

public interface AiFallbackCacheService {

    String buildInputHash(AiFallbackCacheContext context);

    QcAiCache findFallbackCache(AiFallbackCacheContext context);

    void saveValidatedGenerated(AiFallbackCacheContext context);
}
```

- [ ] **Step 3: 写哈希实现骨架**

```java
package com.jhict.quality.service.support.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.config.AiDegradationCacheProperties;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.service.api.AiCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class AiFallbackCacheServiceImpl implements AiFallbackCacheService {

    @Resource
    private AiDegradationCacheProperties properties;

    @Resource
    private AiCacheService aiCacheService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String buildInputHash(AiFallbackCacheContext context) {
        if (context == null || context.getInputSnapshot() == null || context.getInputSnapshot().isEmpty()) {
            return null;
        }
        Map<String, Object> stable = new LinkedHashMap<>();
        stable.put("assessmentType", context.getAssessmentType());
        stable.put("businessType", context.getBusinessType());
        stable.put("businessId", context.getBusinessId());
        stable.put("promptVersion", context.getPromptVersion());
        stable.put("modelName", context.getModelName());
        stable.put("inputSnapshot", context.getInputSnapshot());
        try {
            String json = objectMapper.writeValueAsString(stable);
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            log.warn("构造AI缓存inputHash失败，assessmentType={}, businessId={}, error={}",
                    context.getAssessmentType(), context.getBusinessId(), e.getMessage());
            return null;
        }
    }

    @Override
    public QcAiCache findFallbackCache(AiFallbackCacheContext context) {
        if (!cacheReadable(context)) {
            return null;
        }
        String inputHash = buildInputHash(context);
        if (!StringUtils.hasText(inputHash)) {
            return null;
        }
        return aiCacheService.findActive(context.getAssessmentType(), context.getBusinessType(),
                context.getBusinessId(), context.getPromptVersion(), inputHash);
    }

    @Override
    public void saveValidatedGenerated(AiFallbackCacheContext context) {
        if (!cacheWritable(context)) {
            return;
        }
        String inputHash = buildInputHash(context);
        if (!StringUtils.hasText(inputHash)) {
            return;
        }
        aiCacheService.saveValidatedGenerated(context, inputHash, properties.effectiveTtlDays());
    }

    private boolean cacheReadable(AiFallbackCacheContext context) {
        return properties.isEnabled()
                && properties.isFallbackOnly()
                && context != null
                && StringUtils.hasText(context.getAssessmentType())
                && StringUtils.hasText(context.getBusinessType())
                && StringUtils.hasText(context.getBusinessId())
                && StringUtils.hasText(context.getPromptVersion());
    }

    private boolean cacheWritable(AiFallbackCacheContext context) {
        return cacheReadable(context)
                && properties.isWriteOnValidatedGenerated()
                && StringUtils.hasText(context.getOutputText());
    }
}
```

- [ ] **Step 4: 写哈希测试**

在 `AiFallbackCacheServiceImplTest` 增加：

```java
@Test
void buildInputHashIsStableForSameSnapshot() {
    AiFallbackCacheServiceImpl service = new AiFallbackCacheServiceImpl();
    ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("judgmentType", "QUALIFIED");
    snapshot.put("evidences", Arrays.asList("Rm:430", "A:30.5"));

    AiFallbackCacheContext context = AiFallbackCacheContext.builder()
            .assessmentType("JUDGMENT_EXPLANATION")
            .businessType("QC_JUDGMENT_RESULT")
            .businessId("jud001")
            .promptVersion("prompt-v1")
            .modelName("deepseek-ai/DeepSeek-V4-Flash")
            .inputSnapshot(snapshot)
            .build();

    assertThat(service.buildInputHash(context)).isEqualTo(service.buildInputHash(context));
}
```

需要 imports：

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
```

- [ ] **Step 5: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=AiFallbackCacheServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/support/ai backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java
git commit -m "feat: 添加AI缓存策略服务"
```

---

### Task 3: AiCacheService 支持新版查询与版本化写入

**Files:**
- Modify: `backend/src/main/java/com/jhict/quality/service/api/AiCacheService.java`
- Modify: `backend/src/main/java/com/jhict/quality/service/impl/AiCacheServiceImpl.java`
- Test: `backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java`

- [ ] **Step 1: 扩展接口**

```java
package com.jhict.quality.service.api;

import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;

public interface AiCacheService {

    QcAiCache findActive(String assessmentType, String businessType, String businessId);

    QcAiCache findActive(String assessmentType, String businessType, String businessId,
                         String promptVersion, String inputHash);

    void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays);
}
```

- [ ] **Step 2: 实现新版查询与写入**

在 `AiCacheServiceImpl` 增加：

```java
@Override
public QcAiCache findActive(String assessmentType, String businessType, String businessId,
                            String promptVersion, String inputHash) {
    return aiCacheMapper.selectList(new LambdaQueryWrapper<QcAiCache>()
                    .eq(QcAiCache::getAssessmentType, assessmentType)
                    .eq(QcAiCache::getBusinessType, businessType)
                    .eq(QcAiCache::getBusinessId, businessId)
                    .eq(QcAiCache::getPromptVersion, promptVersion)
                    .eq(QcAiCache::getInputHash, inputHash)
                    .eq(QcAiCache::getEnabled, 1)
                    .and(w -> w.isNull(QcAiCache::getExpiryTime)
                            .or()
                            .ge(QcAiCache::getExpiryTime, LocalDateTime.now()))
                    .orderByDesc(QcAiCache::getCreateDateTime))
            .stream()
            .findFirst()
            .orElse(null);
}

@Override
@Transactional(rollbackFor = Exception.class)
public void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays) {
    List<QcAiCache> activeCaches = aiCacheMapper.selectList(new LambdaQueryWrapper<QcAiCache>()
            .eq(QcAiCache::getAssessmentType, context.getAssessmentType())
            .eq(QcAiCache::getBusinessType, context.getBusinessType())
            .eq(QcAiCache::getBusinessId, context.getBusinessId())
            .eq(QcAiCache::getPromptVersion, context.getPromptVersion())
            .eq(QcAiCache::getInputHash, inputHash)
            .eq(QcAiCache::getEnabled, 1));
    for (QcAiCache cache : activeCaches) {
        cache.setEnabled(0);
        aiCacheMapper.updateById(cache);
    }

    QcAiCache cache = new QcAiCache();
    cache.setCacheKey(buildCacheKey(context, inputHash));
    cache.setAssessmentType(context.getAssessmentType());
    cache.setBusinessType(context.getBusinessType());
    cache.setBusinessId(context.getBusinessId());
    cache.setPromptVersion(context.getPromptVersion());
    cache.setInputHash(inputHash);
    cache.setCachedOutput(context.getOutputText());
    cache.setReferencesJson(context.getStructuredOutput());
    cache.setConfidenceLabel(context.getConfidenceLabel());
    cache.setConfidenceScore(context.getConfidenceScore());
    cache.setDegradationSource("CACHE");
    cache.setEnabled(1);
    cache.setExpiryTime(LocalDateTime.now().plusDays(ttlDays));
    aiCacheMapper.insert(cache);
}

private String buildCacheKey(AiFallbackCacheContext context, String inputHash) {
    return context.getAssessmentType() + ":" + context.getBusinessType() + ":"
            + context.getBusinessId() + ":" + context.getPromptVersion() + ":" + inputHash;
}
```

需要 imports：

```java
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
```

- [ ] **Step 3: 增加服务层交互测试**

在 `AiFallbackCacheServiceImplTest` 中用 Mockito 验证 `saveValidatedGenerated` 调用下层服务：

```java
@Test
void saveValidatedGeneratedSkipsWhenCacheDisabled() {
    AiDegradationCacheProperties properties = new AiDegradationCacheProperties();
    properties.setEnabled(false);
    AiCacheService aiCacheService = Mockito.mock(AiCacheService.class);

    AiFallbackCacheServiceImpl service = new AiFallbackCacheServiceImpl();
    ReflectionTestUtils.setField(service, "properties", properties);
    ReflectionTestUtils.setField(service, "aiCacheService", aiCacheService);
    ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("question", "为什么合格");

    service.saveValidatedGenerated(AiFallbackCacheContext.builder()
            .assessmentType("CERT_QA")
            .businessType("COIL")
            .businessId("Z001001")
            .promptVersion("prompt-v1")
            .modelName("deepseek-ai/DeepSeek-V4-Flash")
            .inputSnapshot(snapshot)
            .outputText("可信回答[1]")
            .confidenceLabel("HIGH")
            .confidenceScore(BigDecimal.valueOf(0.85D))
            .build());

    Mockito.verifyNoInteractions(aiCacheService);
}
```

需要 imports：

```java
import com.jhict.quality.service.api.AiCacheService;
import org.mockito.Mockito;
import java.math.BigDecimal;
```

- [ ] **Step 4: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=AiFallbackCacheServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/api/AiCacheService.java backend/src/main/java/com/jhict/quality/service/impl/AiCacheServiceImpl.java backend/src/test/java/com/jhict/quality/service/support/ai/AiFallbackCacheServiceImplTest.java
git commit -m "feat: 支持AI缓存版本化读写"
```

---

### Task 4: 判定解释接入缓存兜底

**Files:**
- Modify: `backend/src/main/java/com/jhict/quality/service/impl/JudgmentServiceImpl.java`
- Test: `backend/src/test/java/com/jhict/quality/service/impl/JudgmentServiceImplTest.java`

- [ ] **Step 1: 注入缓存策略服务**

在 `JudgmentServiceImpl` 增加字段：

```java
@Resource
private AiFallbackCacheService aiFallbackCacheService;
```

需要 import：

```java
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import com.jhict.quality.service.support.ai.AiFallbackCacheService;
```

- [ ] **Step 2: 生成缓存上下文**

新增私有方法：

```java
private AiFallbackCacheContext buildJudgmentCacheContext(QcJudgmentResultVO vo,
                                                        QcJudgmentResult result,
                                                        String outputText) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("judgmentType", result.getJudgmentType());
    snapshot.put("matchedStandardIds", result.getMatchedStandardIds());
    snapshot.put("evidences", vo.getEvidences());
    snapshot.put("citations", vo.getCitations());
    snapshot.put("citationMissing", vo.getCitationMissing());
    return AiFallbackCacheContext.builder()
            .assessmentType("JUDGMENT_EXPLANATION")
            .businessType("QC_JUDGMENT_RESULT")
            .businessId(result.getId())
            .promptVersion(judgmentExplanationPromptBuilder.activePromptVersion())
            .modelName(modelGateway.provider())
            .inputSnapshot(snapshot)
            .outputText(outputText)
            .structuredOutput(toJsonQuietly(vo.getCitations()))
            .references(vo.getCitations())
            .confidenceLabel(vo.getConfidenceLabel())
            .confidenceScore(vo.getConfidenceScore() == null ? null : BigDecimal.valueOf(vo.getConfidenceScore()))
            .build();
}

private String toJsonQuietly(Object value) {
    try {
        return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
        return null;
    }
}
```

需要 imports：

```java
import java.util.LinkedHashMap;
import java.util.Map;
```

- [ ] **Step 3: 模型通过校验后写缓存**

在 `fillGeneratedExplanationIfPossible` 的 `trusted` 分支设置完 `vo.setAiExplanation(trusted)` 后加入：

```java
aiFallbackCacheService.saveValidatedGenerated(buildJudgmentCacheContext(vo, result, trusted));
```

- [ ] **Step 4: 模型失败或校验失败后查缓存**

在调用 `tryStructuredExplanation` 前加入：

```java
QcAiCache fallbackCache = aiFallbackCacheService.findFallbackCache(
        buildJudgmentCacheContext(vo, result, null));
if (fallbackCache != null && StringUtils.hasText(fallbackCache.getCachedOutput())) {
    vo.setAiExplanation(fallbackCache.getCachedOutput());
    vo.setAiExplanationTrace("CACHE_HIT");
    log.info("AI判定解释命中降级缓存，judgmentId={}", judgmentId);
    return AiExplanationSource.GENERATED;
}
```

需要 import：

```java
import com.jhict.quality.entity.QcAiCache;
```

- [ ] **Step 5: 写测试**

创建或补充 `JudgmentServiceImplTest`，用 Mockito 验证模型校验失败时调用缓存服务。测试核心断言：

```java
@Test
void generatedExplanationUsesFallbackCacheWhenModelOutputRejected() {
    // 使用 ReflectionTestUtils 注入 mock ModelGateway、AiFallbackCacheService、ObjectMapper、promptBuilder。
    // 模型返回 success=true 但 content 无引用标记，CitationReferenceSupport 会拒绝。
    // aiFallbackCacheService.findFallbackCache 返回 cachedOutput="缓存解释[1]"。
    // 调用私有 fillGeneratedExplanationIfPossible 可通过 ReflectionTestUtils.invokeMethod。
    // 断言 vo.getAiExplanation() 等于 "缓存解释[1]"，trace 为 CACHE_HIT。
}
```

- [ ] **Step 6: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=JudgmentServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/impl/JudgmentServiceImpl.java backend/src/test/java/com/jhict/quality/service/impl/JudgmentServiceImplTest.java
git commit -m "feat: 判定解释支持缓存兜底"
```

---

### Task 5: 质保书问答改为失败后缓存兜底

**Files:**
- Modify: `backend/src/main/java/com/jhict/quality/service/impl/CertQaServiceImpl.java`
- Test: `backend/src/test/java/com/jhict/quality/service/impl/CertQaServiceImplTest.java`

- [ ] **Step 1: 注入缓存策略服务**

```java
@Resource
private AiFallbackCacheService aiFallbackCacheService;
```

- [ ] **Step 2: 移除前置缓存返回**

将 `answer` 初始化后当前的前置 `tryCache` 逻辑移到模型失败分支。删除或停用这段前置返回：

```java
CertQaAnswerVO cached = tryCache(cmd, citations, basis);
if (cached != null) {
    return cached;
}
```

- [ ] **Step 3: 构造缓存上下文**

新增：

```java
private AiFallbackCacheContext buildCertQaCacheContext(CertQaQueryCmd cmd,
                                                       QcJudgmentResultVO judgment,
                                                       CertQaAnswerVO answer,
                                                       String outputText) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("question", cmd.getQuestion());
    snapshot.put("coilNo", cmd.getCoilNo());
    snapshot.put("batchNo", cmd.getBatchNo());
    snapshot.put("judgmentId", judgment.getJudgmentId());
    snapshot.put("judgmentType", judgment.getJudgmentType());
    snapshot.put("indicatorBasis", answer.getIndicatorBasis());
    snapshot.put("citations", answer.getCitations());
    return AiFallbackCacheContext.builder()
            .assessmentType("CERT_QA")
            .businessType(StringUtils.hasText(cmd.getCoilNo()) ? "COIL" : "BATCH")
            .businessId(StringUtils.hasText(cmd.getCoilNo()) ? cmd.getCoilNo() : cmd.getBatchNo())
            .promptVersion(certQaPromptBuilder.activePromptVersion())
            .modelName(modelGateway.provider())
            .inputSnapshot(snapshot)
            .outputText(outputText)
            .structuredOutput(toJsonQuietly(answer.getCitations()))
            .references(answer.getCitations())
            .confidenceLabel(answer.getConfidenceLabel())
            .confidenceScore("HIGH".equals(answer.getConfidenceLabel())
                    ? BigDecimal.valueOf(0.85D) : BigDecimal.valueOf(0.65D))
            .build();
}
```

- [ ] **Step 4: 生成成功后写缓存**

在 `trusted` 分支中 `persistCertQaAssessment` 前加入：

```java
aiFallbackCacheService.saveValidatedGenerated(buildCertQaCacheContext(cmd, judgment, answer, trusted));
```

- [ ] **Step 5: 失败分支查缓存**

在 `applyRuleDegradation(answer, citations, ruleAnswer);` 前加入：

```java
QcAiCache fallbackCache = aiFallbackCacheService.findFallbackCache(
        buildCertQaCacheContext(cmd, judgment, answer, null));
if (fallbackCache != null && StringUtils.hasText(fallbackCache.getCachedOutput())) {
    answer.setAnswer(parseCacheAnswer(fallbackCache.getCachedOutput()));
    answer.setConfidenceLabel(fallbackCache.getConfidenceLabel());
    answer.setDegradationSource("CACHE");
    answer.setCacheHit(true);
    persistCertQaAssessment(cmd, judgment, answer, false);
    return;
}
```

- [ ] **Step 6: 写测试**

测试目标：

```java
@Test
void certQaDoesNotUseCacheBeforeModelAttempt() {
    // mock aiFallbackCacheService.findFallbackCache 返回缓存。
    // mock modelGateway.chat 返回通过引用校验的内容。
    // 调用 query。
    // 断言返回 GENERATED，不是 CACHE。
    // verify aiFallbackCacheService.findFallbackCache 没有被调用或只在失败分支调用。
}

@Test
void certQaUsesCacheAfterModelFailure() {
    // mock modelGateway.chat 返回 failure。
    // mock aiFallbackCacheService.findFallbackCache 返回 cachedOutput。
    // 调用 query。
    // 断言 degradationSource=CACHE，cacheHit=true。
}
```

- [ ] **Step 7: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=CertQaServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 8: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/impl/CertQaServiceImpl.java backend/src/test/java/com/jhict/quality/service/impl/CertQaServiceImplTest.java
git commit -m "feat: 质保书问答改为缓存兜底"
```

---

### Task 6: 标准 RAG 接入缓存兜底

**Files:**
- Modify: `backend/src/main/java/com/jhict/quality/service/impl/StandardRagServiceImpl.java`
- Modify: `backend/src/test/java/com/jhict/quality/service/impl/StandardRagServiceImplTest.java`

- [ ] **Step 1: 注入缓存策略服务**

```java
@Resource
private AiFallbackCacheService aiFallbackCacheService;
```

- [ ] **Step 2: 构造 RAG 缓存上下文**

```java
private AiFallbackCacheContext buildStandardRagCacheContext(StandardRagQueryCmd cmd,
                                                           List<StandardRagSourceVO> sources,
                                                           String outputText,
                                                           String confidenceLabel,
                                                           Double confidenceScore) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("query", cmd.getQuery());
    snapshot.put("sourceTypes", cmd.getSourceTypes());
    snapshot.put("customerId", cmd.getCustomerId());
    snapshot.put("variety", cmd.getVariety());
    snapshot.put("grade", cmd.getGrade());
    snapshot.put("indicatorCode", cmd.getIndicatorCode());
    snapshot.put("effectiveDate", cmd.getEffectiveDate());
    snapshot.put("sources", sources);
    return AiFallbackCacheContext.builder()
            .assessmentType("STANDARD_RAG")
            .businessType("STANDARD_RAG_QUERY")
            .businessId(cmd.getQuery())
            .promptVersion(standardRagPromptBuilder.activePromptVersion())
            .modelName(modelGateway.provider())
            .inputSnapshot(snapshot)
            .outputText(outputText)
            .structuredOutput(toJsonQuietly(CitationReferenceSupport.toAiReferences(sources)))
            .references(CitationReferenceSupport.toAiReferences(sources))
            .confidenceLabel(confidenceLabel)
            .confidenceScore(confidenceScore == null ? null : BigDecimal.valueOf(confidenceScore))
            .build();
}
```

- [ ] **Step 3: 模型输出可信时写缓存**

在 `buildDegradationRequest` 或 `query` 中判断 `isGroundedGeneratedOutput(modelResponse, sources)` 为 true 后，调用：

```java
aiFallbackCacheService.saveValidatedGenerated(buildStandardRagCacheContext(
        cmd, sources, modelResponse.getContent(), "HIGH", HIGH_CONFIDENCE_SCORE));
```

- [ ] **Step 4: 模型失败或输出不可信时查缓存**

在调用 `aiDegradationService.resolveAiOutput` 前，如果 `generatedOutput` 为空，先查缓存：

```java
AiFallbackCacheContext cacheContext = buildStandardRagCacheContext(
        cmd, sources, null,
        modelResponse != null && modelResponse.isSuccess() ? "HIGH" : "MEDIUM",
        modelResponse != null && modelResponse.isSuccess() ? HIGH_CONFIDENCE_SCORE : MEDIUM_CONFIDENCE_SCORE);
QcAiCache fallbackCache = aiFallbackCacheService.findFallbackCache(cacheContext);
if (fallbackCache != null && StringUtils.hasText(fallbackCache.getCachedOutput())) {
    StandardRagAnswerVO answer = buildAnswer(cmd, sources,
            buildCacheDegradationResult(cmd, fallbackCache, sources), queryVector);
    answer.setCacheHit(true);
    return answer;
}
```

新增：

```java
private AiDegradationResultVO buildCacheDegradationResult(StandardRagQueryCmd cmd,
                                                          QcAiCache cache,
                                                          List<StandardRagSourceVO> sources) {
    AiDegradationResultVO result = new AiDegradationResultVO();
    result.setAssessmentType("STANDARD_RAG");
    result.setBusinessType("STANDARD_RAG_QUERY");
    result.setBusinessId(cmd.getQuery());
    result.setOutputText(cache.getCachedOutput());
    result.setDegradationSource(AiDegradationSource.CACHE.getCode());
    result.setDegradationReason("模型输出不可用，命中同输入快照缓存");
    result.setConfidenceLabel(cache.getConfidenceLabel());
    result.setConfidenceScore(cache.getConfidenceScore() == null ? null : cache.getConfidenceScore().doubleValue());
    result.setAuthoritative(true);
    result.setReferences(CitationReferenceSupport.toAiReferences(sources));
    return result;
}
```

- [ ] **Step 5: 写测试**

在 `StandardRagServiceImplTest` 增加：

```java
@Test
void queryUsesFallbackCacheWhenModelOutputFailsCitationValidation() {
    // 准备可用 sources。
    // modelGateway.chat 返回 success=true 但内容没有 [1] 引用。
    // aiFallbackCacheService.findFallbackCache 返回 cachedOutput。
    // 调用 query。
    // 断言 answer.degradationSource == CACHE，answer.cacheHit == true。
}
```

- [ ] **Step 6: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=StandardRagServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/impl/StandardRagServiceImpl.java backend/src/test/java/com/jhict/quality/service/impl/StandardRagServiceImplTest.java
git commit -m "feat: 标准RAG支持缓存兜底"
```

---

### Task 7: 让步风险说明接入保守缓存策略

**Files:**
- Modify: `backend/src/main/java/com/jhict/quality/service/impl/ConcessionRiskServiceImpl.java`
- Test: `backend/src/test/java/com/jhict/quality/service/impl/ConcessionRiskServiceImplTest.java`

- [ ] **Step 1: 注入缓存策略服务**

```java
@Resource
private AiFallbackCacheService aiFallbackCacheService;
```

- [ ] **Step 2: 增加最低可信校验**

新增：

```java
private boolean trustedConcessionNarrative(String content, ConcessionRiskAssessmentVO result) {
    if (!StringUtils.hasText(content)) {
        return false;
    }
    if (!StringUtils.hasText(result.getRiskLevel()) || !content.contains(result.getRiskLevel())) {
        return false;
    }
    if (!result.getBlockingReasons().isEmpty()
            && result.getBlockingReasons().stream().noneMatch(content::contains)) {
        return false;
    }
    if (!result.getSuggestedConditions().isEmpty()
            && result.getSuggestedConditions().stream().noneMatch(content::contains)) {
        return false;
    }
    return true;
}
```

- [ ] **Step 3: 构造缓存上下文**

```java
private AiFallbackCacheContext buildConcessionCacheContext(QcJudgmentResultVO judgment,
                                                           ConcessionRiskAssessmentVO result,
                                                           String outputText) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("judgmentId", judgment.getJudgmentId());
    snapshot.put("judgmentType", judgment.getJudgmentType());
    snapshot.put("riskLevel", result.getRiskLevel());
    snapshot.put("mustReview", result.getMustReview());
    snapshot.put("missingInfo", result.getMissingInfo());
    snapshot.put("suggestedConditions", result.getSuggestedConditions());
    snapshot.put("blockingReasons", result.getBlockingReasons());
    snapshot.put("evidenceRefs", result.getEvidenceRefs());
    snapshot.put("alternativeStocks", result.getAlternativeStocks());
    return AiFallbackCacheContext.builder()
            .assessmentType("CONCESSION_RISK")
            .businessType("QC_JUDGMENT_RESULT")
            .businessId(judgment.getJudgmentId())
            .promptVersion(concessionRiskPromptBuilder.activePromptVersion())
            .modelName(modelGateway.provider())
            .inputSnapshot(snapshot)
            .outputText(outputText)
            .structuredOutput(toJsonQuietly(result))
            .confidenceLabel(result.getConfidenceLabel())
            .confidenceScore(result.getConfidenceScore() == null ? null : BigDecimal.valueOf(result.getConfidenceScore()))
            .build();
}
```

- [ ] **Step 4: 成功才写缓存，失败再兜底**

修改 `fillAiWordingIfAvailable`：

```java
ModelChatResponse response = modelGateway.chat(
        concessionRiskPromptBuilder.build(judgment.getJudgmentId(), result));
if (response != null && response.isSuccess()
        && trustedConcessionNarrative(response.getContent(), result)) {
    result.setNarrativeExplanation(response.getContent());
    result.setDegradationSource("GENERATED");
    aiFallbackCacheService.saveValidatedGenerated(
            buildConcessionCacheContext(judgment, result, response.getContent()));
    return;
}
QcAiCache fallbackCache = aiFallbackCacheService.findFallbackCache(
        buildConcessionCacheContext(judgment, result, null));
if (fallbackCache != null && StringUtils.hasText(fallbackCache.getCachedOutput())) {
    result.setNarrativeExplanation(fallbackCache.getCachedOutput());
    result.setConfidenceLabel(fallbackCache.getConfidenceLabel());
    result.setConfidenceScore(fallbackCache.getConfidenceScore() == null
            ? result.getConfidenceScore() : fallbackCache.getConfidenceScore().doubleValue());
    result.setDegradationSource("CACHE");
}
```

- [ ] **Step 5: 写测试**

```java
@Test
void concessionRiskDoesNotCacheUntrustedModelNarrative() {
    // modelGateway.chat 返回 success=true 但内容不包含风险等级。
    // 调用 assess。
    // verify aiFallbackCacheService.saveValidatedGenerated 未调用。
    // 断言 degradationSource 保持 RULE_TEMPLATE 或命中 CACHE。
}
```

- [ ] **Step 6: 运行测试**

Run:

```bash
cd backend && mvn -Dtest=ConcessionRiskServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: 提交**

```bash
git add backend/src/main/java/com/jhict/quality/service/impl/ConcessionRiskServiceImpl.java backend/src/test/java/com/jhict/quality/service/impl/ConcessionRiskServiceImplTest.java
git commit -m "feat: 让步风险支持保守缓存兜底"
```

---

### Task 8: 审计字段与端到端回归

**Files:**
- Modify: touched service tests from Tasks 4-7 as needed
- Verify: `backend/scripts/migrations/20260621_01_ai_quality_p0_schema.sql`

- [ ] **Step 1: 确认表结构无需新增字段**

检查 `qc_ai_cache` 已包含：

```text
prompt_version
input_hash
cached_output
references_json
confidence_label
confidence_score
degradation_source
enabled
expiry_time
```

如果当前部署库来自旧 schema，追加迁移脚本：

```sql
ALTER TABLE `qc_ai_cache`
  ADD COLUMN `prompt_version` VARCHAR(50) DEFAULT NULL COMMENT 'Prompt版本',
  ADD COLUMN `input_hash` VARCHAR(128) DEFAULT NULL COMMENT '输入哈希',
  ADD COLUMN `expiry_time` DATETIME DEFAULT NULL COMMENT '过期时间';

CREATE INDEX `idx_ai_cache_prompt_hash`
  ON `qc_ai_cache` (`assessment_type`, `business_type`, `business_id`, `prompt_version`, `input_hash`, `enabled`);
```

本仓库当前 P0 schema 已有这些字段时，不新增迁移脚本。

- [ ] **Step 2: 确认缓存命中审计**

缓存命中的业务服务在 `persist...Assessment` 前必须设置：

```java
answerOrResult.setDegradationSource("CACHE");
answerOrResult.setCacheHit(true);
```

对没有 `cacheHit` 字段的 VO，在创建 `AiAssessmentCreateCmd` 时设置：

```java
createCmd.setDegradationSource("CACHE");
createCmd.setCacheHit(1);
```

- [ ] **Step 3: 运行相关单测**

Run:

```bash
cd backend && mvn -Dtest=AiFallbackCacheServiceImplTest,StandardRagServiceImplTest,CertQaServiceImplTest,JudgmentServiceImplTest,ConcessionRiskServiceImplTest test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: 运行后端完整测试**

Run:

```bash
cd backend && mvn test
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java backend/src/test/java backend/src/main/resources/application.yml backend/scripts/migrations
git commit -m "test: 完善AI降级缓存回归覆盖"
```

---

## 自检清单

- 配置设计覆盖 `enabled`、`mode`、`ttl-days`、`write-on-validated-generated`。
- 缓存只在 `FALLBACK_ONLY` 下作为失败兜底，不抢占成功模型输出。
- 模型通过校验后写缓存，缓存写入失败不影响 `GENERATED` 返回。
- 缓存命中要求 `promptVersion + inputHash`，并检查 `enabled` 与 `expiryTime`。
- 缓存写入采用新增版本并停用旧版本。
- 判定解释、质保书问答、标准 RAG、让步风险均有接入任务。
- 让步风险不会缓存未经最低可信校验的话术。
- 测试覆盖配置、hash、读写、业务场景成功与失败分支。
