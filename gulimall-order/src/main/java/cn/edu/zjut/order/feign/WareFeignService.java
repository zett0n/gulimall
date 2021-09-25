package cn.edu.zjut.order.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.FareVO;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    // 查询 sku 是否有库存
    @PostMapping("ware/waresku/hasstock")
    R hasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/wareinfo/fare/{addrId}")
    FareVO getFare(@PathVariable("addrId") Long addrId);

    // @RequestMapping("ware/waresku/lock/order")
    // R orderLockStock(@RequestBody WareSkuLockVo itemVos);
}
