package cn.edu.zjut.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    // 设置跨域过滤器
    // 产生这个Bean对象交给容器管理
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();

        // CorsConfiguration 类代表跨域配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 配置跨域相关信息
        // 允许哪些请求头跨域
        corsConfiguration.addAllowedHeader("*");
        // 允许哪些请求方式跨域（get,put...)
        corsConfiguration.addAllowedMethod("*");
        // 允许哪些请求来源跨域
        corsConfiguration.addAllowedOrigin("*");
        // 允许携带cookie跨域
        corsConfiguration.setAllowCredentials(true);

        // 对所有路径，注册上文的跨域配置
        configSource.registerCorsConfiguration("/**", corsConfiguration);

        // CorsWebFilter 需要传 CorsConfigurationSource，也就是跨域配置源
        return new CorsWebFilter(configSource);
    }
}
