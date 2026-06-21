package com.jhict.quality.ai.prompt;

import lombok.Data;

@Data
public class PromptTemplate {

    private String promptKey;

    private String version;

    private String description;

    private String system;

    private String userTemplate;

    public String renderUser(String... keyValuePairs) {
        if (userTemplate == null) {
            return "";
        }
        String result = userTemplate;
        if (keyValuePairs == null || keyValuePairs.length < 2) {
            return result;
        }
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            String placeholder = "{{" + keyValuePairs[i] + "}}";
            String value = keyValuePairs[i + 1] != null ? keyValuePairs[i + 1] : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
