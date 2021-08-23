package cn.edu.zjut.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

