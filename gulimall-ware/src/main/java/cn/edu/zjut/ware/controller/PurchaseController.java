package cn.edu.zjut.ware.controller;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.ware.entity.PurchaseEntity;
import cn.edu.zjut.ware.service.PurchaseService;
import cn.edu.zjut.ware.vo.MergeVO;
import cn.edu.zjut.ware.vo.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    // 04、合并采购需求
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO mergeVO) {
        this.purchaseService.mergePurchase(mergeVO);
        return R.ok();
    }

    // 05、查询未领取的采购单
    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = this.purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }

    // 06、领取采购单
    @PostMapping("/received")
    public R received(@RequestBody List<Long> purchaseIds) {
        this.purchaseService.received(purchaseIds);
        return R.ok();
    }

    // 07、完成采购
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO purchaseDoneVO) {
        this.purchaseService.done(purchaseDoneVO);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = this.purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setUpdateTime(new Date());
        purchase.setCreateTime(new Date());

        this.purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase) {
        this.purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
