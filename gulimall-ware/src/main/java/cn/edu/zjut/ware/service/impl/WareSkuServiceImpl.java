package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.dto.OrderDTO;
import cn.edu.zjut.common.dto.SkuHasStockDTO;
import cn.edu.zjut.common.dto.mq.StockLockDTO;
import cn.edu.zjut.common.enume.OrderStatusEnum;
import cn.edu.zjut.common.enume.WareTaskStatusEnum;
import cn.edu.zjut.common.exception.NoStockException;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.OrderItemVO;
import cn.edu.zjut.common.vo.WareSkuLockVO;
import cn.edu.zjut.ware.dao.WareSkuDao;
import cn.edu.zjut.ware.entity.WareOrderTaskDetailEntity;
import cn.edu.zjut.ware.entity.WareOrderTaskEntity;
import cn.edu.zjut.ware.entity.WareSkuEntity;
import cn.edu.zjut.ware.feign.OrderFeignService;
import cn.edu.zjut.ware.feign.ProductFeignService;
import cn.edu.zjut.ware.service.WareOrderTaskDetailService;
import cn.edu.zjut.ware.service.WareOrderTaskService;
import cn.edu.zjut.ware.service.WareSkuService;
import cn.edu.zjut.ware.vo.SkuLockVO;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;
import static cn.edu.zjut.common.constant.DefaultConstant.STOCK_UNLOCK;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
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

            // 远程查询 sku 的名字，如果失败，整个事务无需回滚
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
     * 锁定库存，锁定库存失败直接抛异常回滚
     */
    @Override
    @Transactional
    public void orderLockStock(WareSkuLockVO wareSkuLockVO) {
        // 订单号
        String orderSn = wareSkuLockVO.getOrderSn();
        // 要锁定的订单项
        List<OrderItemVO> OrderItemVOs = wareSkuLockVO.getLocks();

        // 保存订单锁定工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(orderSn)
                .setCreateTime(new Date());
        this.wareOrderTaskService.save(wareOrderTaskEntity);
        // 初始化 mq 消息传输 DTO
        StockLockDTO stockLockDTO = new StockLockDTO();
        stockLockDTO.setTaskId(wareOrderTaskEntity.getId());

        // 批量查询库存，封装锁定库存需要的属性返回
        List<SkuLockVO> skuLockVOS = OrderItemVOs.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();

            Long skuId = item.getSkuId();
            Integer count = item.getCount();
            // 找出所有库存大于商品数的仓库
            List<Long> wareIds = this.baseMapper.listWareIdsHasStock(skuId, count);

            skuLockVO.setSkuId(skuId)
                    .setSkuNum(count)
                    .setWareIds(wareIds);

            return skuLockVO;
        }).collect(Collectors.toList());

        // 批量锁定库存
        for (SkuLockVO skuLockVO : skuLockVOS) {
            Long skuId = skuLockVO.getSkuId();
            List<Long> wareIds = skuLockVO.getWareIds();
            Integer skuNum = skuLockVO.getSkuNum();
            // 标记该商品项是否已被锁定
            boolean stocked = false;

            // 没有任何仓库有该商品库存，抛异常回滚
            if (wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }

            // 简单考虑，只针对某个仓库一起锁库存，不会在多个仓库组合锁库存
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuId, skuNum, wareId);
                // 锁定成功
                if (count == 1) {
                    stocked = true;
                    // 保存库存锁定工作单
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(skuId)
                            .setSkuName("")
                            .setSkuNum(skuNum)
                            .setTaskId(wareOrderTaskEntity.getId())
                            .setWareId(wareId)
                            .setLockStatus(WareTaskStatusEnum.LOCKED.getCode());
                    this.wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    // 一条订单锁定工作单对应多条库存锁定工作单
                    stockLockDTO.getDetailIds().add(wareOrderTaskDetailEntity.getId());
                    // 跳出循环
                    break;
                }
                // 当前仓库锁定失败（库存不够），换个仓库
            }
            // 遍历每个有库存的仓库，每个仓库库存都不够，抛异常回滚
            if (!stocked) {
                throw new NoStockException(skuId);
            }
        }
        // 整个订单锁库存成功，向 mq 发送消息
        // 如果库存不够抛异常，订单模块可以感知到异常同样回滚，mq 无需发消息
        this.rabbitTemplate.convertAndSend("stock-event-exchange", "stock.lock", stockLockDTO);
    }


    /**
     * 解锁库存前，需要进行一些检查
     */
    @Override
    @Transactional
    public void checkBeforeUnlock(StockLockDTO stockLockDTO) {
        // 查询订单锁定工作单
        Long taskId = stockLockDTO.getTaskId();
        WareOrderTaskEntity wareOrderTaskEntity = this.wareOrderTaskService.getById(taskId);

        // 获得订单号后远程查询订单状态
        String orderSn = wareOrderTaskEntity.getOrderSn();
        R r = this.orderFeignService.infoByOrderSn(orderSn);
        if (r.getCode() != R_SUCCESS_CODE) {
            // TODO 自定义异常
            // 如果远程调用失败，抛出异常，拒接消息后再重新消费
            throw new RuntimeException("远程调用订单服务失败！");
        }
        OrderDTO orderDTO = r.parseObjectFromMap("order", new TypeReference<OrderDTO>() {
        });

        // 如果没有这个订单（远程锁定库存后【订单模块】发生异常回滚） || 订单已取消（用户超时未支付被系统关单），需要解锁该订单所有占用的库存
        if (orderDTO == null || Objects.equals(orderDTO.getStatus(), OrderStatusEnum.CANCLED.getCode())) {
            // 获取库存锁定工作单
            List<Long> detailIds = stockLockDTO.getDetailIds();

            // 为保证幂等性（防止消息重复消费多解锁库存），只有当库存工作单处于被锁定的情况下才进行解锁
            // 在库存锁定工作单中筛选
            List<WareOrderTaskDetailEntity> detailEntities = this.wareOrderTaskDetailService.getBaseMapper()
                    .selectList(new QueryWrapper<WareOrderTaskDetailEntity>()
                            .eq("lock_status", WareTaskStatusEnum.LOCKED.getCode())
                            .in("id", detailIds));

            unlockStock(detailEntities);
        }
        // 如果订单是其他状态，什么都不做，让消息被消费掉
    }


    /**
     * 处理【订单服务】关单后立即发送的消息，检验是否要解锁库存
     * <p>
     * 【只依赖订单服务消息的问题】
     * 订单服务卡顿（队列很满？），导致订单状态到期了还是不变，此时库存解锁消息被放行，消息也被 ack 提交，导致再也无法解锁那一部分库存
     * 因此需要让【订单服务】在超时后也要发一条消息作为补偿
     */
    @Override
    @Transactional
    public void checkBeforeUnlock(OrderDTO orderDTO) {
        // 查询订单锁定工作单，因为是订单模块立即发送的新消息，无需再查询数据库
        String orderSn = orderDTO.getOrderSn();
        WareOrderTaskEntity taskEntity = this.wareOrderTaskService.getByOrderSn(orderSn);

        // 为保证幂等性，只有当库存工作单处于被锁定的情况下才进行解锁
        // 在库存锁定工作单中筛选
        List<WareOrderTaskDetailEntity> detailEntities = this.wareOrderTaskDetailService
                .list(new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskEntity.getId())
                        .eq("lock_status", WareTaskStatusEnum.LOCKED.getCode()));

        unlockStock(detailEntities);
    }

    /**
     * 解锁库存并标记已解锁状态
     */
    private void unlockStock(List<WareOrderTaskDetailEntity> detailEntities) {
        // 修改库存工作单锁定状态
        List<WareOrderTaskDetailEntity> collect = detailEntities.stream().peek(detailEntity -> {
            detailEntity.setLockStatus(WareTaskStatusEnum.UNLOCKED.getCode());
            // 数据库中解锁库存数据
            this.baseMapper.unlockStock(detailEntity.getSkuId(), detailEntity.getSkuNum(), detailEntity.getWareId());
        }).collect(Collectors.toList());

        this.wareOrderTaskDetailService.updateBatchById(collect);
    }

}