package com.jhict.quality.service.support.prompt;

import org.springframework.core.io.DefaultResourceLoader;

public final class PromptRegistryTestSupport {

    private PromptRegistryTestSupport() {
    }

    public static PromptRegistry createLoadedRegistry() {
        PromptRegistry registry = new PromptRegistry(new PromptProperties(), new DefaultResourceLoader());
        registry.load();
        return registry;
    }
}
