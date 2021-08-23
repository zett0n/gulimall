package cn.edu.zjut.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.common.utils.PageUtils;
import cn.edu.zjut.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

