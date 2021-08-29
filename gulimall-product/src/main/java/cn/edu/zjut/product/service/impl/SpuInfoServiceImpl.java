package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.ProductConstant;
import cn.edu.zjut.common.to.SkuReductionTO;
import cn.edu.zjut.common.to.SpuBoundTO;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.dao.SpuInfoDao;
import cn.edu.zjut.product.entity.*;
import cn.edu.zjut.product.feign.CouponFeignService;
import cn.edu.zjut.product.service.*;
import cn.edu.zjut.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    // @Resource
    // private SkuImagesService skuImagesService;

    // @Resource
    // private SpuInfoDescService spuInfoDescService;

    // @Resource
    // private AttrGroupService attrGroupService;

    // @Resource
    // private SkuSaleAttrValueService skuSaleAttrValueService;

    // @Resource
    // private SeckillFeignService seckillFeignService;

    // @Autowired
    // private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    // TODO 高可用完善
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVO spuSaveVO) {
        // 1、保存 spu 基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVO, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);     //TODO 这里没有考虑分布式下主键自增
        // 这里 save 后拿到了 spu 的 id
        Long spuId = spuInfoEntity.getId();

        // 2、保存 Spu 的描述图片 pms_spu_info_desc
        List<String> decripts = spuSaveVO.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", decripts));
        this.spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3、保存 Spu 的图片集 pms_spu_images
        List<String> imgUrls = spuSaveVO.getImages();
        this.spuImagesService.saveImages(spuId, imgUrls);

        // 4、保存 Spu 的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVO.getBaseAttrs();

        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();

            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            AttrEntity attrEntity = this.attrService.getById(baseAttr.getAttrId());

            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            productAttrValueEntity.setSpuId(spuId);

            return productAttrValueEntity;
        }).collect(Collectors.toList());

        this.productAttrValueService.saveProductAttr(collect);

        //5、[跨服务]保存 spu 的积分信息 [gulimall-sms] sms_spu_bounds
        SpuBoundTO spuBoundTO = new SpuBoundTO();
        Bounds bounds = spuSaveVO.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundTO);
        spuBoundTO.setSpuId(spuId);

        R r1 = this.couponFeignService.saveSpuBounds(spuBoundTO);
        if (r1.getCode() != ProductConstant.REnum.R_SUCCESS_CODE.getVal()) {
            this.log.error("远程保存spu积分信息失败");
        }

        // 6、保存当前 spu 对应的所有 sku 信息
        List<Skus> skus = spuSaveVO.getSkus();
        if (skus == null || skus.isEmpty()) {
            return;
        }
        skus.forEach(sku -> {
            // 6.1、sku 基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);

            // 冗余存储
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());

            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setSpuId(spuId);

            // 保存默认图片
            String defaultImg = "";
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg() == ProductConstant.SkuEnum.SKU_DEFAULT_IMG.getVal()) {
                    defaultImg = image.getImgUrl();
                }
            }
            skuInfoEntity.setSkuDefaultImg(defaultImg);

            this.skuInfoService.saveSkuInfo(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();

            // 6.2、sku 图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(img -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setImgUrl(img.getImgUrl());
                skuImagesEntity.setDefaultImg(img.getDefaultImg());

                return skuImagesEntity;
                // 没有图片路径的无需保存
            }).filter(skuImagesEntity -> {
                // 返回 false 就过滤掉数据，返回 true 留下数据
                return StringUtils.isNotEmpty(skuImagesEntity.getImgUrl());
            }).collect(Collectors.toList());
            this.skuImagesService.saveBatch(skuImagesEntities);

            // 6.3、sku 销售属性 pms_sku_sale_attr_value
            List<Attr> attrs = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);

                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());

            this.skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

            // 7、sku 优惠、满减信息 [gulimall-sms] sms_sku_ladder, sms_sku_full_reduction, sms_member_price
            SkuReductionTO skuReductionTO = new SkuReductionTO();
            BeanUtils.copyProperties(sku, skuReductionTO);
            skuReductionTO.setSkuId(skuId);

            // 如果没有优惠的信息，无需调用优惠服务
            if (skuReductionTO.getFullCount() > 0 || skuReductionTO.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                R r2 = this.couponFeignService.saveSkuReduction(skuReductionTO);
                if (r2.getCode() != ProductConstant.REnum.R_SUCCESS_CODE.getVal()) {
                    this.log.error("远程保存sku优惠信息失败");
                }
            }
        });

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}