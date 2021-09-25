package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.SpuInfoEntity;
import cn.edu.zjut.product.vo.SpuSaveVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * spu信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVO spuSaveVO);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    // 商品上架
    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

