package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

