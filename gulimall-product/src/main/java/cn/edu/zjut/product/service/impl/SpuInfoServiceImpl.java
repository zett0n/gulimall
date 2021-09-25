package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.ProductConstant;
import cn.edu.zjut.common.dto.SkuHasStockDTO;
import cn.edu.zjut.common.dto.SkuReductionDTO;
import cn.edu.zjut.common.dto.SpuBoundDTO;
import cn.edu.zjut.common.dto.es.SkuEsDTO;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.dao.SpuInfoDao;
import cn.edu.zjut.product.entity.*;
import cn.edu.zjut.product.feign.CouponFeignService;
import cn.edu.zjut.product.feign.SearchFeignService;
import cn.edu.zjut.product.feign.WareFeignService;
import cn.edu.zjut.product.service.*;
import cn.edu.zjut.product.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.*;


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

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    // TODO 高可用完善
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVO spuSaveVO) {

        // 1、保存 spu 基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVO, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date())
                .setUpdateTime(new Date());

        // TODO 这里没有考虑分布式下主键自增
        this.saveBaseSpuInfo(spuInfoEntity);

        // 这里 save 后拿到了 spu 的 id
        Long spuId = spuInfoEntity.getId();


        // 2、保存 Spu 的描述图片 pms_spu_info_desc
        List<String> decripts = spuSaveVO.getDecript();

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId)
                .setDecript(String.join(",", decripts));

        this.spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);


        // 3、保存 Spu 的图片集 pms_spu_images
        List<String> imgUrls = spuSaveVO.getImages();
        this.spuImagesService.saveImages(spuId, imgUrls);


        // 4、保存 Spu 的规格参数 pms_product_attr_value
        List<BaseAttrs> productAttrValueEntities = spuSaveVO.getBaseAttrs();

        List<ProductAttrValueEntity> collect = productAttrValueEntities.stream()
                .map(baseAttr -> {
                    ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();

                    productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                    AttrEntity attrEntity = this.attrService.getById(baseAttr.getAttrId());

                    productAttrValueEntity.setAttrName(attrEntity.getAttrName())
                            .setAttrValue(baseAttr.getAttrValues())
                            .setQuickShow(baseAttr.getShowDesc())
                            .setSpuId(spuId);

                    return productAttrValueEntity;
                })
                .collect(Collectors.toList());

        this.productAttrValueService.saveProductAttr(collect);


        //5、[跨服务]保存 spu 的积分信息 [gulimall-sms] sms_spu_bounds
        SpuBoundDTO spuBoundDTO = new SpuBoundDTO();
        Bounds bounds = spuSaveVO.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundDTO);
        spuBoundDTO.setSpuId(spuId);

        R r1 = this.couponFeignService.saveSpuBounds(spuBoundDTO);
        if (r1.getCode() != R_SUCCESS_CODE) {
            this.log.error("远程保存spu积分信息失败");
        }


        // 6、保存当前 spu 对应的所有 skuInfoEntity 信息
        List<Skus> skus = spuSaveVO.getSkus();
        if (skus == null || skus.isEmpty()) {
            return;
        }

        skus.forEach(sku -> {
            // 6.1、skuInfoEntity 基本信息 pms_sku_info
            // 保存默认图片
            String defaultImg = "";
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg() == SKU_DEFAULT_IMG) {
                    defaultImg = image.getImgUrl();
                }
            }

            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            // 冗余存储
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId())
                    .setCatalogId(spuInfoEntity.getCatalogId())
                    .setSaleCount(0L)
                    .setSpuId(spuId)
                    .setSkuDefaultImg(defaultImg);

            this.skuInfoService.saveSkuInfo(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();

            // 6.2、skuInfoEntity 图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream()
                    .map(img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId)
                                .setImgUrl(img.getImgUrl())
                                .setDefaultImg(img.getDefaultImg());

                        return skuImagesEntity;
                    })
                    // 没有图片路径的无需保存
                    .filter(skuImagesEntity -> {
                        // 返回 false 就过滤掉数据，返回 true 留下数据
                        return StringUtils.isNotEmpty(skuImagesEntity.getImgUrl());
                    })
                    .collect(Collectors.toList());

            this.skuImagesService.saveBatch(skuImagesEntities);

            // 6.3、skuInfoEntity 销售属性 pms_sku_sale_attr_value
            List<Attr> attrs = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream()
                    .map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuId);

                        return skuSaleAttrValueEntity;
                    })
                    .collect(Collectors.toList());

            this.skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);


            // 7、skuInfoEntity 优惠、满减信息 [gulimall-sms] sms_sku_ladder, sms_sku_full_reduction, sms_member_price
            SkuReductionDTO skuReductionDTO = new SkuReductionDTO();
            BeanUtils.copyProperties(sku, skuReductionDTO);
            skuReductionDTO.setSkuId(skuId);

            // 如果没有优惠的信息，无需调用优惠服务
            if (skuReductionDTO.getFullCount() > 0 || skuReductionDTO.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                R r2 = this.couponFeignService.saveSkuReduction(skuReductionDTO);
                if (r2.getCode() != R_SUCCESS_CODE) {
                    this.log.error("远程保存sku优惠信息失败");
                }
            }
        });

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /*
         * status: 2
         * key:模糊查询
         * brandId: 9
         * catelogId: 225
         */
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and((w) -> w.eq("id", key).or().like("spu_name", key));
        }

        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) &&
                !String.valueOf(ID_SELECT_ALL).equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) &&
                !String.valueOf(ID_SELECT_ALL).equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }


    /**
     * 封装产品上架相关信息发送给 Es
     *
     * @param spuId 品牌 id
     */
    // @GlobalTransactional(rollbackFor = Exception.class)
    // @Transactional(rollbackFor = Exception.class)
    @Override
    public void up(Long spuId) {
        // 1、查出当前 spuId 对应的所有 sku 相关信息
        // 1.1、从 pms_sku_info 查出 sku 基本信息
        List<SkuInfoEntity> skuInfoEntities = this.skuInfoService.getSkusBySpuId(spuId);

        // 1.2、根据 spuId 查出当前 sku 的所有可被检索的规格属性
        // 从 pms_product_attr_value 查出 spuId 对应的所有属性（包括不可检索）
        List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueService.baseAttrListforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream()
                .map(ProductAttrValueEntity::getAttrId)
                .collect(Collectors.toList());

        // 将所有属性带入 pms_attr 筛选出可被检索的规格属性
        List<Long> searchAttrIds = this.attrService.selectSearchAttrIds(attrIds);

        // 转换为Set集合，元素数量没减少但是查询效率 O(n) -> O(1)
        Set<Long> searchAttrIdSet = new HashSet<>(searchAttrIds);

        // 封装 SkuEsDTO.Attrs
        List<SkuEsDTO.Attrs> attrsList = productAttrValueEntities.stream()
                .filter(item -> searchAttrIdSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsDTO.Attrs attrs = new SkuEsDTO.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());

        // 1.3、发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        List<Long> skuIds = skuInfoEntities.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());

        try {
            R r = this.wareFeignService.hasStock(skuIds);

            // TypeReference 的构造器访问权限为 protected（子类或者同包）
            // 这里用匿名内部类（继承 TypeReference 的匿名子类）实现跨包调用
            TypeReference<List<SkuHasStockDTO>> typeReference = new TypeReference<List<SkuHasStockDTO>>() {
            };

            stockMap = r.parseObjectFromMap("data", typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockDTO::getSkuId, SkuHasStockDTO::getHasStock));
        } catch (Exception e) {
            this.log.error("库存服务查询异常：原因{}", e);
        }

        // 2、封装每个 sku 的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsDTO> upProducts = skuInfoEntities.stream()
                .map(skuInfoEntity -> {
                    // 组装需要的数据
                    SkuEsDTO skuEsDTO = new SkuEsDTO();
                    BeanUtils.copyProperties(skuInfoEntity, skuEsDTO);
                    skuEsDTO.setSkuPrice(skuInfoEntity.getPrice())
                            .setSkuImg(skuInfoEntity.getSkuDefaultImg());

                    // 设置库存信息
                    if (finalStockMap == null) {
                        // 默认有库存（方便调试时服务器超时报异常的情况）
                        skuEsDTO.setHasStock(true);
                    } else {
                        skuEsDTO.setHasStock(finalStockMap.get(skuInfoEntity.getSkuId()));
                    }

                    // TODO 应该为可定制
                    // 热度评分（默认0）
                    skuEsDTO.setHotScore(0L);

                    // 查询品牌和分类的名字信息
                    BrandEntity brandEntity = this.brandService.getById(skuEsDTO.getBrandId());
                    skuEsDTO.setBrandName(brandEntity.getName())
                            .setBrandImg(brandEntity.getLogo());

                    CategoryEntity categoryEntity = this.categoryService.getById(skuEsDTO.getCatalogId());
                    skuEsDTO.setCatalogName(categoryEntity.getName());

                    // 设置可检索属性
                    skuEsDTO.setAttrs(attrsList);

                    return skuEsDTO;
                })
                .collect(Collectors.toList());

        // 3、将数据发给 gulimall-search Es 进行保存
        // 因为 Es 在保存商品时设置了 id，因此重复保存只会更新属性值（接口幂等）
        R r = this.searchFeignService.productStatusUp(upProducts);

        if (r.getCode() == R_SUCCESS_CODE) {
            // 远程调用成功，修改当前 spu 的状态 (pms_spu_info 的 publish_status 和 update_time)
            // 数据库修改失败怎么保证 Es 和 MySQL 数据一致性？由于 Es 接口幂等性，再重新上传商品即可
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getVal());
        } else {
            // 远程调用失败
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = this.skuInfoService.getById(skuId);
        SpuInfoEntity spu = this.getById(skuInfoEntity.getSpuId());
        BrandEntity brandEntity = this.brandService.getById(spu.getBrandId());
        spu.setBrandName(brandEntity.getName());

        return spu;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

}