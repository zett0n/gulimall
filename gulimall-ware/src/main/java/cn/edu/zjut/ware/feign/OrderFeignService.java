package cn.edu.zjut.ware.feign;

import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("order/order/infoByOrderSn/{OrderSn}")
    R infoByOrderSn(@PathVariable("OrderSn") String OrderSn);

}
