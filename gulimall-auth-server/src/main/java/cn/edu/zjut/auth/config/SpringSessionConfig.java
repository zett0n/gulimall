package cn.edu.zjut.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class SpringSessionConfig {
    // @Bean
    // public CookieSerializer cookieSerializer() {
    //
    //     DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
    //
    //     //放大作用域
    //     cookieSerializer.setDomainName("gulimall.com");
    //     cookieSerializer.setCookieName("GULI-SESSION");
    //
    //     return cookieSerializer;
    // }

    /**
     * 修改 Spring Session Redis 序列化方式
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
