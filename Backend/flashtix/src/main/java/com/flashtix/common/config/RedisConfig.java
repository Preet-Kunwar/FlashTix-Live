package com.flashtix.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Tells Redis to save the Keys as plain strings (e.g., "event:1:tickets")
        template.setKeySerializer(new StringRedisSerializer());

        // Tells Redis to save the Values as JSON (perfect for caching numbers or entire objects later)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}