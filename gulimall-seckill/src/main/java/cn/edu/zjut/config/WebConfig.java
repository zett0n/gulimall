package cn.edu.zjut.config;

import cn.edu.zjut.interceptor.SeckillLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SeckillLoginInterceptor seckillLoginInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.seckillLoginInterceptor)
                // 立即抢购时判断是否登录
                .addPathPatterns("/seckill/kill");
    }
}
