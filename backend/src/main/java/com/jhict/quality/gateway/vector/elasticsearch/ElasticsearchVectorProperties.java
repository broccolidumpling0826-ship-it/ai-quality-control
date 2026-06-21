package com.jhict.quality.gateway.vector.elasticsearch;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Environment-backed configuration for the Elasticsearch vector gateway.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.vector")
public class ElasticsearchVectorProperties {

    private boolean enabled = false;

    private String host = "http://localhost:9200";

    private String username;

    private String password;

    private String standardIndex = "quality-standard-clauses";

    private Integer timeoutMillis = 5000;
}
