package cn.edu.zjut.order.feign;

import cn.edu.zjut.common.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@FeignClient("gulimall-cart")
public interface CartFeignService {

    @RequestMapping("/getCheckedItems")
    List<OrderItemVO> getCheckedItems();
}
