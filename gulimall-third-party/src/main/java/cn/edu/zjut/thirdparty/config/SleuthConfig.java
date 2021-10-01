package cn.edu.zjut.thirdparty.config;

import brave.sampler.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 解决引入 zipkin 后微服务无法启动问题
 * 参考：https://www.jianshu.com/p/1442b7cb4f5f
 * 加入 zipkin 相关依赖后，项目启动过程 main 线程被阻塞，是 zipkin 相关的采样器 Sampler 的初始化和 Spring 创建 redis 连接实例产生了死锁
 */
@Configuration
public class SleuthConfig {
    @Value("${spring.sleuth.sampler.probability}")
    private String probability;

    /**
     * 自定义一个 Sampler
     */
    @Bean
    public Sampler defaultSampler() throws Exception {
        Float f = new Float(this.probability);
        SamplerProperties samplerProperties = new SamplerProperties();
        samplerProperties.setProbability(f);
        ProbabilityBasedSampler sampler = new ProbabilityBasedSampler(samplerProperties);
        return sampler;
    }
}
