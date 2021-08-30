package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.ware.dao.WareSkuDao;
import cn.edu.zjut.ware.entity.WareSkuEntity;
import cn.edu.zjut.ware.feign.ProductFeignService;
import cn.edu.zjut.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    // TODO @Transactional?
    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", skuId)
                        .eq("ware_id", wareId)
        );

        // 判断如果还没有这个库存记录新增
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            this.wareSkuDao.addStock(skuId, wareId, skuNum);
        } else {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();

            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(DefaultConstant.STOCK_UNLOCK);

            // 远程查询sku的名字，如果失败，整个事务无需回滚
            // 1、自己catch异常
            // 2. TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R r = this.productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) r.get("skuInfo");

                if (r.getCode() == DefaultConstant.R_SUCCESS_CODE) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                this.log.error("远程查询 skuName 失败");
            }
            this.wareSkuDao.insert(wareSkuEntity);
        }
    }

}