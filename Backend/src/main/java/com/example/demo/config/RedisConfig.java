package com.hms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for the HMS application.
 * <p>
 * Configures serialisers for Redis keys and values.
 * Uses String serialisation for keys and JSON serialisation for complex values.
 */
@Configuration
public class RedisConfig {

    /**
     * Configures a {@link StringRedisTemplate} for simple string key-value operations.
     * Used for JWT tokens, OTP codes, and reset tokens.
     *
     * @param connectionFactory the Redis connection factory (auto-configured by Spring)
     * @return a configured StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Configures a general-purpose {@link RedisTemplate} with String keys and JSON values.
     * Used for caching complex objects like search results.
     *
     * @param connectionFactory the Redis connection factory
     * @return a configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
