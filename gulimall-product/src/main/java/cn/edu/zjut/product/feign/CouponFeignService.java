package cn.edu.zjut.product.feign;

import cn.edu.zjut.common.to.SkuReductionTO;
import cn.edu.zjut.common.to.SpuBoundTO;
import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * CouponFeignService.saveSpuBounds(spuBoundTo); 调用过程
     * 1）、@RequestBody 将这个对象转为json。
     * 2）、找到 gulimall-coupon 服务，将上一步转的json放在请求体位置，给 /coupon/spubounds/save 发送请求。
     * 3）、对方服务收到请求。请求体里有json数据。
     * (@RequestBody SpuBoundsEntity spuBounds)；将请求体的json转为SpuBoundsEntity；
     * 只要json数据模型是兼容的。双方服务无需使用同一个to
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTO spuBoundTO);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}
