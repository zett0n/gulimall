package cn.edu.zjut.search.api;

import cn.edu.zjut.common.dto.es.SkuEsDTO;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author: zhangshuaiyin
 * @date: 2021/3/11 15:22
 */
@Slf4j
@RequestMapping(value = "search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     */
    @PostMapping(value = "/product")
    public R productStatusUp(@RequestBody List<SkuEsDTO> skuEsDTOs) {
        boolean hasFailures;
        try {
            hasFailures = this.productSaveService.productStatusUp(skuEsDTOs);
        } catch (IOException e) {
            // Es 连不上等问题
            log.error("ElasticSaveController - 商品上架错误: ", e);
            return R.error(EmBizError.ES_CONNECTION_EXCEPTION);
        }

        if (hasFailures) {
            // sku 数据存在问题
            return R.error(EmBizError.PRODUCT_UP_EXCEPTION);
        }

        return R.ok();
    }
}
