package com.jhict.quality.service.support.prompt;

import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.service.support.rag.CitationReferenceSupport.CitationPromptStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptRegistryTest {

    private PromptRegistry registry;

    @BeforeEach
    void setUp() {
        registry = PromptRegistryTestSupport.createLoadedRegistry();
    }

    @Test
    void resolve_shouldLoadAllActiveScenes() {
        assertEquals("judgment-explanation-v3",
                registry.resolve(PromptScene.JUDGMENT_EXPLANATION).getVersion());
        assertEquals("standard-rag-p0-v3", registry.resolve(PromptScene.STANDARD_RAG).getVersion());
        assertEquals("cert-qa-v4", registry.resolve(PromptScene.CERT_QA).getVersion());
        assertEquals("concession-risk-v1", registry.resolve(PromptScene.CONCESSION_RISK).getVersion());
        assertEquals("vision-ocr-v1", registry.resolve(PromptScene.VISION_OCR).getVersion());
    }

    @Test
    void resolveVersion_shouldReturnSpecificTemplate() {
        PromptTemplate template = registry.resolveVersion(PromptScene.CERT_QA, "cert-qa-v4");
        assertNotNull(template.getSystemPrompt());
        assertTrue(template.getConditionalSnippets().containsKey("append-non-final"));
    }

    @Test
    void load_shouldRegisterCitationRules() {
        String rules = registry.getCitationRule(CitationPromptStyle.STANDARD_RAG);
        assertNotNull(rules);
        assertTrue(rules.contains("[1]"));
        StringBuilder prompt = new StringBuilder();
        CitationReferenceSupport.appendCitationAnswerRules(prompt, CitationPromptStyle.STANDARD_RAG);
        assertEquals(rules, prompt.toString());
    }

    @Test
    void load_shouldFailWhenManifestMissing() {
        PromptProperties properties = new PromptProperties();
        properties.setBasePath("classpath:missing-ai-prompts/");
        PromptRegistry missing = new PromptRegistry(properties, new DefaultResourceLoader());
        assertThrows(Exception.class, missing::load);
    }
}
