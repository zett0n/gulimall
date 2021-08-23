package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

