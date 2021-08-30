package cn.edu.zjut.ware.feign;

import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 两种调用方式:
     * 1. 直接调用: @FeignClient("gulimall-gateway"), /product/skuinfo/info/{skuId}
     * 2. 让所有请求过网关: @FeignClient("gulimall-product"), /api/product/skuinfo/info/{skuId}
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
