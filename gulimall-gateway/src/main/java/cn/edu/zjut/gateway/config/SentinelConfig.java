package cn.edu.zjut.gateway.config;

import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelConfig {
    public SentinelConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            // 网关限流了请求就会调用此回调
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                // TODO 网关sentinel控制uri控流
                // TODO mono，webflux，响应式编程
                R error = R.error(EmBizError.SYSTEM_BUSY_EXCEPTION);
                String errorStr = JSON.toJSONString(error);

                return ServerResponse.ok().body(Mono.just(errorStr), String.class);
            }
        });
    }

}
