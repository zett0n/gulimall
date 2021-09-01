package cn.edu.zjut.coupon.service;

import cn.edu.zjut.common.dto.SkuReductionDTO;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.coupon.entity.SkuFullReductionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 04:38:47
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionDTO skuReductionDTO);
}

