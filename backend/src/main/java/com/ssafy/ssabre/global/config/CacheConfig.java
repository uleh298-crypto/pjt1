package com.ssafy.ssabre.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.ssafy.ssabre.global.redis")
public class CacheConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Jackson 3 - JavaTimeModule 자동 포함됨
                ObjectMapper objectMapper = JsonMapper.builder()
                                .activateDefaultTyping(
                                                BasicPolymorphicTypeValidator.builder()
                                                                .allowIfBaseType(Object.class)
                                                                .build(),
                                                DefaultTyping.NON_FINAL,
                                                JsonTypeInfo.As.PROPERTY)
                                .build();

                GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(objectMapper);

                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(3))
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer));

                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
                cacheConfigurations.put("hotPosts", defaultConfig.entryTtl(Duration.ofMinutes(3)));

                RedisCacheConfiguration longTermConfig = defaultConfig.entryTtl(Duration.ofDays(1));
                cacheConfigurations.put("boards", longTermConfig);
                cacheConfigurations.put("campuses", longTermConfig);
                cacheConfigurations.put("classes", longTermConfig);
                cacheConfigurations.put("campusClasses", longTermConfig);

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}