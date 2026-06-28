package com.jhict.quality.service.support.prompt;

import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.service.support.rag.CitationReferenceSupport.CitationPromptStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@Component
@Slf4j
public class PromptRegistry {

    private final PromptProperties properties;
    private final ResourceLoader resourceLoader;

    private final Map<PromptScene, String> activeVersions = new EnumMap<>(PromptScene.class);
    private final Map<PromptScene, SceneManifestEntry> sceneEntries = new EnumMap<>(PromptScene.class);
    private final Map<PromptScene, Map<String, PromptTemplate>> templatesByScene = new EnumMap<>(PromptScene.class);
    private final Map<CitationPromptStyle, String> citationRules = new EnumMap<>(CitationPromptStyle.class);

    public PromptRegistry(PromptProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void load() {
        reload();
    }

    public void reload() {
        activeVersions.clear();
        sceneEntries.clear();
        templatesByScene.clear();
        citationRules.clear();

        Map<String, Object> manifest = readYaml(manifestResource());
        loadCitationRules(manifest);
        loadScenes(manifest);

        Map<CitationPromptStyle, String> registeredRules = new EnumMap<>(CitationPromptStyle.class);
        registeredRules.putAll(citationRules);
        CitationReferenceSupport.registerCitationRules(registeredRules);

        log.info("Prompt registry loaded: scenes={}, citations={}",
                activeVersions.size(), citationRules.size());
    }

    public PromptTemplate resolve(PromptScene scene) {
        String version = activeVersions.get(scene);
        if (!StringUtils.hasText(version)) {
            throw new ServiceException("Prompt scene not configured: " + scene);
        }
        return resolveVersion(scene, version);
    }

    public PromptTemplate resolveVersion(PromptScene scene, String version) {
        Map<String, PromptTemplate> versions = templatesByScene.get(scene);
        if (versions == null) {
            throw new ServiceException("Prompt scene not loaded: " + scene);
        }
        PromptTemplate template = versions.get(version);
        if (template == null) {
            throw new ServiceException("Prompt template not found: " + scene + " / " + version);
        }
        return template;
    }

    public String activeVersion(PromptScene scene) {
        return activeVersions.get(scene);
    }

    public String getCitationRule(CitationPromptStyle style) {
        return citationRules.get(style);
    }

    private void loadCitationRules(Map<String, Object> manifest) {
        Map<String, Object> shared = asMap(manifest.get("shared"));
        Map<String, Object> citationRulePaths = asMap(shared.get("citationRules"));
        for (Map.Entry<String, Object> entry : citationRulePaths.entrySet()) {
            CitationPromptStyle style = CitationPromptStyle.valueOf(entry.getKey());
            String relativePath = String.valueOf(entry.getValue());
            citationRules.put(style, readRequiredText(relativePath));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScenes(Map<String, Object> manifest) {
        Map<String, Object> defaults = asMap(manifest.get("defaults"));
        Double defaultTemperature = toDouble(defaults.get("temperature"));

        Map<String, Object> scenes = asMap(manifest.get("scenes"));
        for (Map.Entry<String, Object> entry : scenes.entrySet()) {
            PromptScene scene = PromptScene.fromManifestKey(entry.getKey());
            Map<String, Object> sceneConfig = asMap(entry.getValue());
            String activeVersion = requiredText(sceneConfig.get("activeVersion"), "activeVersion", scene);
            activeVersions.put(scene, activeVersion);

            SceneManifestEntry manifestEntry = SceneManifestEntry.builder()
                    .scene(scene)
                    .activeVersion(activeVersion)
                    .businessType(requiredText(sceneConfig.get("businessType"), "businessType", scene))
                    .citationStyle(parseCitationStyle(sceneConfig.get("citationStyle")))
                    .temperature(firstDouble(sceneConfig.get("temperature"), defaultTemperature))
                    .maxTokens(toInteger(sceneConfig.get("maxTokens")))
                    .build();
            sceneEntries.put(scene, manifestEntry);

            PromptTemplate template = loadTemplate(scene, manifestEntry, activeVersion);
            Map<String, PromptTemplate> versions = templatesByScene.computeIfAbsent(scene, key -> new HashMap<>());
            versions.put(activeVersion, template);
        }
    }

    private PromptTemplate loadTemplate(PromptScene scene, SceneManifestEntry entry, String version) {
        String folder = scene.getResourceFolder();
        String prefix = folder + "/" + version;
        String systemPrompt = readRequiredText(prefix + ".system.txt");
        String userSkeleton = readOptionalText(prefix + ".user-skeleton.txt");
        String userPrompt = readOptionalText(prefix + ".user-prompt.txt");
        Map<String, String> snippets = loadConditionalSnippets(folder, prefix + ".meta.yaml", version);

        return PromptTemplate.builder()
                .scene(scene)
                .version(version)
                .businessType(entry.getBusinessType())
                .systemPrompt(systemPrompt)
                .userSkeleton(userSkeleton)
                .userPrompt(userPrompt)
                .citationStyle(entry.getCitationStyle())
                .temperature(entry.getTemperature())
                .maxTokens(entry.getMaxTokens())
                .conditionalSnippets(snippets)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadConditionalSnippets(String folder, String metaPath, String version) {
        Map<String, Object> meta = readYaml(resource(folder + "/" + version + ".meta.yaml"));
        Map<String, Object> snippetPaths = asMap(meta.get("snippets"));
        if (snippetPaths.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> snippets = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : snippetPaths.entrySet()) {
            String relativePath = folder + "/" + String.valueOf(entry.getValue());
            snippets.put(entry.getKey(), readRequiredText(relativePath));
        }
        return snippets;
    }

    private Resource manifestResource() {
        return resource("manifest.yaml");
    }

    private Resource resource(String relativePath) {
        String base = normalizeBasePath(properties.getBasePath());
        return resourceLoader.getResource(base + relativePath);
    }

    private String normalizeBasePath(String basePath) {
        if (!StringUtils.hasText(basePath)) {
            return "classpath:ai-prompts/";
        }
        return basePath.endsWith("/") ? basePath : basePath + "/";
    }

    private Map<String, Object> readYaml(Resource resource) {
        try (InputStream inputStream = openRequired(resource)) {
            Object loaded = new Yaml().load(inputStream);
            return loaded == null ? Collections.emptyMap() : asMap(loaded);
        } catch (IOException ex) {
            throw new ServiceException("Failed to read prompt yaml: " + resource, ex);
        }
    }

    private String readRequiredText(String relativePath) {
        Resource resource = resource(relativePath);
        try (InputStream inputStream = openRequired(resource)) {
            return readStream(inputStream);
        } catch (IOException ex) {
            throw new ServiceException("Failed to read prompt resource: " + relativePath, ex);
        }
    }

    private String readOptionalText(String relativePath) {
        Resource resource = resource(relativePath);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return readStream(inputStream);
        } catch (IOException ex) {
            throw new ServiceException("Failed to read prompt resource: " + relativePath, ex);
        }
    }

    private InputStream openRequired(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new ServiceException("Missing prompt resource: " + resource.getDescription());
        }
        return resource.getInputStream();
    }

    private static String readStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private static CitationPromptStyle parseCitationStyle(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        return CitationPromptStyle.valueOf(String.valueOf(value));
    }

    private static String requiredText(Object value, String field, PromptScene scene) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw new ServiceException("Missing manifest field " + field + " for scene " + scene);
        }
        return String.valueOf(value);
    }

    private static Double firstDouble(Object primary, Double fallback) {
        Double parsed = toDouble(primary);
        return parsed != null ? parsed : fallback;
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(String.valueOf(value));
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    @lombok.Value
    @lombok.Builder
    private static class SceneManifestEntry {
        PromptScene scene;
        String activeVersion;
        String businessType;
        CitationPromptStyle citationStyle;
        Double temperature;
        Integer maxTokens;
    }
}
