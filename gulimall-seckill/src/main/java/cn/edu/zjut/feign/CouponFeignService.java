package cn.edu.zjut.feign;

import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("coupon/seckillsession/list/in/{days}")
    R listInDays(@PathVariable("days") Integer days);

}
