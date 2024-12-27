package com.example.maziyyah.mood_tracker.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.maziyyah.mood_tracker.util.Utils;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public JedisConnectionFactory createJedisConnFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisUsername.trim().length() > 0) {
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }

        JedisClientConfiguration jcc = JedisClientConfiguration.builder().build();
        JedisConnectionFactory jcf = new JedisConnectionFactory(config, jcc);

        return jcf;
    }

    @Bean
    @Qualifier(Utils.template01) // for using with Map, List and value?
    public RedisTemplate<String, Object> template01(JedisConnectionFactory jcf) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jcf);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    @Qualifier(Utils.template02) // for using with Objects
    public RedisTemplate<String, String> template02(JedisConnectionFactory jcf) {
        
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jcf);
        
        // Use StringRedisSerializer for key serialization
        template.setKeySerializer(new StringRedisSerializer());
        // Use StringRedisSerializer for value serialization
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

}
