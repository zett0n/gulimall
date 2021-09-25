package cn.edu.zjut.cart.config;

import cn.edu.zjut.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CartInterceptor cartInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(this.cartInterceptor).addPathPatterns("/**");
    }
}
