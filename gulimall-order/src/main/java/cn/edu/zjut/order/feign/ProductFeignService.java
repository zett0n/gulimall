package cn.edu.zjut.order.feign;

import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    
    @GetMapping("product/spuinfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
