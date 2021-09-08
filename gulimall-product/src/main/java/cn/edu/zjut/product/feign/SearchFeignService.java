package cn.edu.zjut.product.feign;

import cn.edu.zjut.common.dto.es.SkuEsDTO;
import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping(value = "search/save/product")
    R productStatusUp(@RequestBody List<SkuEsDTO> skuEsDTOs);
}
