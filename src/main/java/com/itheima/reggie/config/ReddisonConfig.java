package com.itheima.reggie.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ReddisonConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;

    @Bean
    public RedissonClient getRedisSon() {
        Config config = new Config();
        String address = new StringBuilder("redis://").append(host).append(":").append(port).toString();
        //创建单例模式的配置
        config.useSingleServer().setAddress(address);
        return Redisson.create(config);
    }

}
