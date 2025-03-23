package com.example.spring_boot_mongodb_docker.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfiguration {

    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:alpine"))
            .withExposedPorts(6379);

    static {
        redisContainer.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setHost(redisContainer.getHost());
        redisProperties.setPort(redisContainer.getFirstMappedPort());
        return redisProperties;
    }
}

