package com.jhict.quality.service.support.prompt;

import org.springframework.util.StringUtils;

import java.util.Map;

final class PromptPlaceholderUtils {

    private PromptPlaceholderUtils() {
    }

    static String apply(String template, Map<String, String> values) {
        if (!StringUtils.hasText(template) || values == null || values.isEmpty()) {
            return template == null ? "" : template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String replacement = entry.getValue() == null ? "" : entry.getValue();
            rendered = rendered.replace("{{" + entry.getKey() + "}}", replacement);
        }
        return rendered;
    }
}
