package cn.edu.zjut.product.vo;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.product.entity.SkuImagesEntity;
import cn.edu.zjut.product.entity.SkuInfoEntity;
import cn.edu.zjut.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString
@Accessors(chain = true)
public class SkuItemVO {

    // 1、sku 基本信息的获取  pms_sku_info
    private SkuInfoEntity info;

    private boolean hasStock = true;

    // 2、sku 的图片信息  pms_sku_images
    private List<SkuImagesEntity> images;

    // 3、获取 spu 的销售属性组合
    private List<SkuItemSaleAttrVO> saleAttr;

    // 4、获取 spu 的介绍
    private SpuInfoDescEntity desc;

    // 5、获取 spu 的规格参数信息
    private List<SpuItemAttrGroupVO> groupAttrs;

    // 6、秒杀商品的优惠信息
    private SeckillSkuRedisDTO seckillSkuRedisDTO;

}
