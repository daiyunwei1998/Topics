package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.host}")
    private String host;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(6379);
        configuration.setPassword(password);

        // Create LettuceClientConfiguration with SSL support
        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigBuilder = LettuceClientConfiguration.builder();
        lettuceClientConfigBuilder.commandTimeout(Duration.ofSeconds(5));

        // Build the LettuceClientConfiguration
        LettuceClientConfiguration lettuceClientConfig = lettuceClientConfigBuilder.build();

        // Return a new LettuceConnectionFactory with the configured SSL settings
        return new LettuceConnectionFactory(configuration, lettuceClientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Set key serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Set value serializer
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return redisTemplate;
    }
}
