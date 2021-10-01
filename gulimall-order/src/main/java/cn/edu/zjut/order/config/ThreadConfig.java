package cn.edu.zjut.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties config) {
        return new ThreadPoolExecutor(
                config.getCoreSize(),
                config.getMaxSize(),
                config.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(config.getQueueLength()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
