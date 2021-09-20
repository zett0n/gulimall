package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.SkuInfoDao;
import cn.edu.zjut.product.entity.SkuImagesEntity;
import cn.edu.zjut.product.entity.SkuInfoEntity;
import cn.edu.zjut.product.entity.SpuInfoDescEntity;
import cn.edu.zjut.product.service.*;
import cn.edu.zjut.product.vo.SkuItemSaleAttrVO;
import cn.edu.zjut.product.vo.SkuItemVO;
import cn.edu.zjut.product.vo.SpuItemAttrGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    // @Autowired
    // private SeckillFeignService seckillFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );
        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    // TODO 用一个vo封装 params 后用 jsr303 做数据校验，或者用 mybatisplus 校验
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /*
         * key: 模糊查询
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((wrapper) -> wrapper.eq("sku_id", key).or().like("sku_name", key));
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId)
                && !String.valueOf(DefaultConstant.ID_SELECT_ALL).equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId)
                && !String.valueOf(DefaultConstant.ID_SELECT_ALL).equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max)) {
            try {
                if (new BigDecimal(max).compareTo(BigDecimal.ZERO) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                // 什么都不做，不追加这个查询条件
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    /**
     * 异步编排
     * 步骤 2~4 都需要步骤 1 先获取 sku 基本信息后才能执行，步骤 5 可以单独执行，最后需要等所有步骤完成返回 VO
     * thenAcceptAsync：异步开启一个新任务，能接受上一步结果，但是无返回值
     */
    @Override
    public SkuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        // 1、sku 基本信息的获取 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = this.getById(skuId);
            skuItemVO.setInfo(info);
            return info;
        }, this.executor);


        // 2、获取 spu 的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVO> saleAttrVOs = this.skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVO.setSaleAttr(saleAttrVOs);
        }, this.executor);


        // 3、获取spu的介绍    pms_spu_info_desc
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = this.spuInfoDescService.getById(res.getSpuId());
            skuItemVO.setDesc(spuInfoDescEntity);
        }, this.executor);


        // 4、获取spu的规格参数信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVO> attrGroupVOs = this.attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVO.setGroupAttrs(attrGroupVOs);
        }, this.executor);


        // 5、sku的图片信息   pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = this.skuImagesService.getImagesBySkuId(skuId);
            skuItemVO.setImages(imagesEntities);
        }, this.executor);


        // 等到所有任务都完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();

        return skuItemVO;
    }
}

// Long spuId = info.getSpuId();
// Long catalogId = info.getCatalogId();

        /* CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
             //3、远程调用查询当前sku是否参与秒杀优惠活动
             R skuSeckilInfo = seckillFeignService.getSkuSeckilInfo(skuId);
             if (skuSeckilInfo.getCode() == 0) {
                 //查询成功
                 SeckillSkuVo seckilInfoData = skuSeckilInfo.getData("data", new TypeReference<SeckillSkuVo>() {
                 });
                 skuItemVO.setSeckillSkuVo(seckilInfoData);

                 if (seckilInfoData != null) {
                     long currentTime = System.currentTimeMillis();
                     if (currentTime > seckilInfoData.getEndTime()) {
                         skuItemVO.setSeckillSkuVo(null);
                     }
                 }
             }
         }, executor);*/
// CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, seckillFuture).get();