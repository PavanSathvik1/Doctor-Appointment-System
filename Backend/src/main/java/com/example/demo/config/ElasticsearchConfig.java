package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Configuration to enable Spring Data Elasticsearch repositories.
 * <p>
 * Ensure Elasticsearch repositories do not conflict with JPA repositories
 * by explicitly restricting the base packages or separating them.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.hms.elasticsearch.doctor")
public class ElasticsearchConfig {
    // Spring Boot auto-configures the Elasticsearch client based on application.yml properties.
    // The @EnableElasticsearchRepositories annotation specifically tells Spring Data
    // to look for ES repositories in the specified package, isolating them from JPA.
}
