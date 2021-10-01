package cn.edu.zjut.config;

import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sentinel v1.7
 * <p>
 * sentinel 保护 feign 远程调用（服务方设置）
 * 流控：限制 qps 等
 * 降级：服务方可以手动设置降级策略，当被降级时，返回自定义异常信息
 */
@Component
@Slf4j
public class SentinelConfig implements BlockExceptionHandler {
    /**
     * 自定义异常返回信息
     */
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        log.warn("【秒杀服务】流量过大，返回异常信息...");

        R r = R.error(EmBizError.SYSTEM_BUSY_EXCEPTION);
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(r));
    }
}
