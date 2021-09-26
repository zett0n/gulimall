package cn.edu.zjut.order.feign;

import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.FareVO;
import cn.edu.zjut.common.vo.WareSkuLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    // 查询 sku 是否有库存
    @PostMapping("ware/waresku/hasstock")
    R hasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/wareinfo/fare/{addrId}")
    FareVO getFare(@PathVariable("addrId") Long addrId);

    @PostMapping("ware/waresku//lock/order")
    R orderLockStock(@RequestBody WareSkuLockVO wareSkuLockVO);
}
