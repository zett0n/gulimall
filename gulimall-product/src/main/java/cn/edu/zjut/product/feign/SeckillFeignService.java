package cn.edu.zjut.product.feign;

import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * sentinel 保护 feign 远程调用（调用方设置）
 * 熔断：远程服务器宕机时，触发熔断回调（fallback）
 * 降级：调用方可以手动设置降级策略，当远程服务器被降级时，触发熔断回调（fallback）
 */
@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    @GetMapping("/seckill/sku/{skuId}")
    R getSeckillSkuInfo(@PathVariable("skuId") Long skuId);

}
