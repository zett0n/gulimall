package cn.edu.zjut.order.config;

import cn.edu.zjut.order.interceptor.OrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private OrderInterceptor orderInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(new OrderInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(this.orderInterceptor).addPathPatterns("/**");
    }
}
