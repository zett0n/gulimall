package cn.edu.zjut.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ConfigurationProperties 绑定配置文件线程池的相关参数
 * @Component 注入容器，这样 ThreadConfig 的入参就可以直接使用
 * @Data get、set 方法
 */
@ConfigurationProperties(prefix = "thread.pool")
@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
    private Integer queueLength;
}
