package cn.edu.zjut.order.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置 feign 拦截器，使得 feign 请求头带上 cookie 信息
 */
@Configuration
@Slf4j
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 原理是通过主线程的 threadlocal 获得当前请求（异步情况下当前线程拿不到主线程的 request，因此产生空指针异常）
                ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

                if (servletRequestAttributes != null) {
                    HttpServletRequest request = servletRequestAttributes.getRequest();
                    // 同步请求头 cookie
                    String cookie = request.getHeader("Cookie");
                    requestTemplate.header("Cookie", cookie);
                }
            }
        };
    }
}
