package cn.edu.zjut.product.feign.fallback;

import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    /**
     * 秒杀服务奔溃时返回错误码
     */
    @Override
    public R getSeckillSkuInfo(Long skuId) {
        log.warn("【商品服务】调用【秒杀服务】超时，触发熔断...");
        return R.error(EmBizError.SYSTEM_BUSY_EXCEPTION);
    }

}
