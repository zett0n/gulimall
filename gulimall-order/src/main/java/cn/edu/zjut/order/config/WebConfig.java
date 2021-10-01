package cn.edu.zjut.order.config;

import cn.edu.zjut.order.interceptor.OrderLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private OrderLoginInterceptor orderLoginInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.orderLoginInterceptor)
                .addPathPatterns("/**")
                // .excludePathPatterns("/payed/**")
                // 库存服务的 listener 收到来自 rabbitmq broker 的请求后通过 feign 调用本微服务接口，不会含有登录信息，因此需要放行
                .excludePathPatterns("/order/order/infoByOrderSn/**");
    }
}
