package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.constant.WareConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.ware.dao.PurchaseDao;
import cn.edu.zjut.ware.entity.PurchaseDetailEntity;
import cn.edu.zjut.ware.entity.PurchaseEntity;
import cn.edu.zjut.ware.service.PurchaseDetailService;
import cn.edu.zjut.ware.service.PurchaseService;
import cn.edu.zjut.ware.service.WareSkuService;
import cn.edu.zjut.ware.vo.MergeVO;
import cn.edu.zjut.ware.vo.PurchaseDoneVO;
import cn.edu.zjut.ware.vo.PurchaseItemDoneVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), new QueryWrapper<>());
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstant.PurchaseStatusEnum.CREATED.getVal())
                        .or().eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getVal())
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();

        // 如果没有提交采购单，自动新建一个采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseUpdate = new PurchaseEntity();

            purchaseUpdate.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getVal());
            purchaseUpdate.setCreateTime(new Date());
            purchaseUpdate.setUpdateTime(new Date());
            this.save(purchaseUpdate);

            purchaseId = purchaseUpdate.getId();
        }

        // 确认采购单状态是 created, assigned 才可以合并
        Integer status = this.baseMapper.selectById(purchaseId).getStatus();
        if (Objects.equals(status, WareConstant.PurchaseStatusEnum.CREATED.getVal())
                || Objects.equals(status, WareConstant.PurchaseStatusEnum.ASSIGNED.getVal())) {

            List<Long> items = mergeVO.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getVal());

                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            this.purchaseDetailService.updateBatchById(collect);

            // 更新采购单日期
            PurchaseEntity purchaseUpdate = new PurchaseEntity();
            purchaseUpdate.setId(purchaseId);
            purchaseUpdate.setUpdateTime(new Date());
            this.updateById(purchaseUpdate);
        }
    }

    @Override
    public void received(List<Long> purchaseIds) {
        // 1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = purchaseIds.stream()
                // TODO stream api 学习
                .map(this::getById)
                .filter(item -> Objects.equals(item.getStatus(), WareConstant.PurchaseStatusEnum.CREATED.getVal())
                        || Objects.equals(item.getStatus(), WareConstant.PurchaseStatusEnum.ASSIGNED.getVal()))
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getVal());
                    item.setUpdateTime(new Date());
                }).collect(Collectors.toList());

        // 2、改变采购单的状态
        this.updateBatchById(purchaseEntities);

        // 3、改变采购项的状态
        purchaseEntities.forEach((item) -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = this.purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(purchaseDetailEntity.getId());
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getVal());
                return entity;
            }).collect(Collectors.toList());

            this.purchaseDetailService.updateBatchById(collect);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVO purchaseDoneVO) {
        // 改变采购项的状态
        boolean purchaseSuccess = true;
        List<PurchaseItemDoneVO> items = purchaseDoneVO.getItems();
        List<PurchaseDetailEntity> purchaseDetailUpdates = new ArrayList<>();

        // TODO 循环查库优化？这里遍历每个成功的采购项目都需要两次数据库io
        // TODO [扩展优化] 数据库设计时没有考虑 wms_purchase_detail 表中增加采购失败原因、应采购与实际采购的字段等
        for (PurchaseItemDoneVO item : items) {
            PurchaseDetailEntity purchaseDetailUpdate = new PurchaseDetailEntity();

            purchaseDetailUpdate.setId(item.getItemId());

            if (Objects.equals(item.getStatus(), WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getVal())) {
                // 采购失败
                purchaseDetailUpdate.setStatus(WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getVal());
                // 检查每个采购项状态，一旦有采购项失败，整个采购单就算失败
                purchaseSuccess = false;
            } else {
                // 采购成功
                purchaseDetailUpdate.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getVal());
                // 到 wms_purchase_detail 表中查出采购项的具体信息
                PurchaseDetailEntity purchaseDetailEntity = this.purchaseDetailService.getById(item.getItemId());
                // 入库到 wms_ware_sku 表
                this.wareSkuService.addStock(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
            }

            purchaseDetailUpdates.add(purchaseDetailUpdate);
        }
        this.purchaseDetailService.updateBatchById(purchaseDetailUpdates);

        // 改变采购单状态
        PurchaseEntity purchaseUpdate = new PurchaseEntity();

        purchaseUpdate.setId(purchaseDoneVO.getId());
        purchaseUpdate.setStatus(purchaseSuccess ? WareConstant.PurchaseStatusEnum.FINISH.getVal() : WareConstant.PurchaseStatusEnum.HAS_ERROR.getVal());
        purchaseUpdate.setUpdateTime(new Date());

        this.updateById(purchaseUpdate);
    }

}