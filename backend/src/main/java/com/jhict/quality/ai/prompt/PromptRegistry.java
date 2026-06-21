package com.jhict.quality.ai.prompt;

import com.jhict.quality.entity.QcAiPromptVersion;
import com.jhict.quality.mapper.QcAiPromptVersionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PromptRegistry {

    private static final String DEFAULT_VERSION = "v1.0.0";

    private final Map<String, PromptTemplate> templateCache = new HashMap<>();

    @Resource
    private QcAiPromptVersionMapper promptVersionMapper;

    @PostConstruct
    public void init() {
        loadClasspathPrompts(DEFAULT_VERSION);
    }

    public PromptTemplate getActiveTemplate(String promptKey) {
        String versionNo = resolveActiveVersion(promptKey);
        String cacheKey = promptKey + ":" + versionNo;
        PromptTemplate cached = templateCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        loadClasspathPrompts(versionNo);
        return templateCache.get(cacheKey);
    }

    public String resolveActiveVersion(String promptKey) {
        try {
            QcAiPromptVersion active = promptVersionMapper.findActiveByPromptKey(promptKey);
            if (active != null && StringUtils.hasText(active.getVersionNo())) {
                return active.getVersionNo();
            }
        } catch (Exception e) {
            log.warn("读取 Prompt 激活版本失败，使用默认版本，promptKey={}", promptKey, e);
        }
        return DEFAULT_VERSION;
    }

    private void loadClasspathPrompts(String versionNo) {
        String[] keys = {"rag-qa", "judgment-explain", "concession-agent", "cert-summary"};
        Yaml yaml = new Yaml();
        for (String key : keys) {
            String path = "prompts/" + versionNo + "/" + key + ".yaml";
            try {
                ClassPathResource resource = new ClassPathResource(path);
                if (!resource.exists()) {
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    Map<String, Object> data = yaml.load(in);
                    PromptTemplate template = mapToTemplate(data);
                    templateCache.put(key + ":" + versionNo, template);
                }
            } catch (Exception e) {
                log.warn("加载 Prompt YAML 失败，path={}", path, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private PromptTemplate mapToTemplate(Map<String, Object> data) {
        PromptTemplate template = new PromptTemplate();
        if (data == null) {
            return template;
        }
        template.setPromptKey(asString(data.get("promptKey")));
        template.setVersion(asString(data.get("version")));
        template.setDescription(asString(data.get("description")));
        template.setSystem(asString(data.get("system")));
        template.setUserTemplate(asString(data.get("userTemplate")));
        return template;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    public Map<String, PromptTemplate> snapshotCache() {
        return Collections.unmodifiableMap(templateCache);
    }
}
