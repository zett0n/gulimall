package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.dto.SkuHasStockDTO;
import cn.edu.zjut.common.exception.NoStockException;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.OrderItemVO;
import cn.edu.zjut.common.vo.WareSkuLockVO;
import cn.edu.zjut.ware.dao.WareSkuDao;
import cn.edu.zjut.ware.entity.WareSkuEntity;
import cn.edu.zjut.ware.feign.ProductFeignService;
import cn.edu.zjut.ware.service.WareSkuService;
import cn.edu.zjut.ware.vo.SkuLockVO;
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
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;
import static cn.edu.zjut.common.constant.DefaultConstant.STOCK_UNLOCK;

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

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

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

            wareSkuEntity.setSkuId(skuId)
                    .setStock(skuNum)
                    .setWareId(wareId)
                    .setStockLocked(STOCK_UNLOCK);

            // 远程查询sku的名字，如果失败，整个事务无需回滚
            // 1、自己catch异常？
            // 2. TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R r = this.productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) r.get("skuInfo");

                if (r.getCode() == R_SUCCESS_CODE) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                this.log.error("远程查询 skuName 失败");
            }
            this.wareSkuDao.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockDTO> hasStock(List<Long> skuIds) {
        // TODO 当前使用了循环查库
        // select sum(stock - stock_locked) as left_stock from wms_ware_sku where sku_id = #{skuId}
        // 优化方案
        // select sku_id, sku_name, sum(stock-stock_locked) as left_stock from wms_ware_sku group by sku_id;

        return skuIds.stream()
                .map(skuId -> {
                    SkuHasStockDTO skuHasStockDTO = new SkuHasStockDTO();
                    Long count = this.baseMapper.getSkuStock(skuId);
                    skuHasStockDTO.setSkuId(skuId)
                            .setHasStock(count != null && count > 0);
                    return skuHasStockDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 简单考虑，只针对某个仓库一起锁库存，不会在多个仓库组合锁库存
     * 锁定库存失败直接抛异常
     */
    @Override
    @Transactional
    public void orderLockStock(WareSkuLockVO wareSkuLockVO) {
        // 获取要锁定的订单项
        List<OrderItemVO> OrderItemVOs = wareSkuLockVO.getLocks();

        // 批量查询库存，封装锁定库存需要的属性返回
        List<SkuLockVO> skuLockVOS = OrderItemVOs.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();

            Long skuId = item.getSkuId();
            Integer count = item.getCount();
            // 找出所有库存大于商品数的仓库
            List<Long> wareIds = this.baseMapper.listWareIdsHasStock(skuId, count);

            skuLockVO.setSkuId(skuId)
                    .setNum(count)
                    .setWareIds(wareIds);

            return skuLockVO;
        }).collect(Collectors.toList());

        // 批量锁定库存
        for (SkuLockVO skuLockVO : skuLockVOS) {
            Long skuId = skuLockVO.getSkuId();
            List<Long> wareIds = skuLockVO.getWareIds();
            Integer num = skuLockVO.getNum();
            // 商品是否被锁住
            boolean stocked = false;

            if (wareIds.isEmpty()) {
                // 没有任何仓库有该商品库存，抛异常回滚
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, num);
                if (count == 1) {
                    // 锁定成功，跳出循环
                    stocked = true;
                    break;
                }
                // 当前仓库锁定失败（库存不够），下个仓库
            }
            if (!stocked) {
                throw new NoStockException(skuId);
            }
        }

    }

}