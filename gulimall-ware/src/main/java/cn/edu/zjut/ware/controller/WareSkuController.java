package cn.edu.zjut.ware.controller;

import cn.edu.zjut.common.dto.SkuHasStockDTO;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.exception.NoStockException;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.WareSkuLockVO;
import cn.edu.zjut.ware.entity.WareSkuEntity;
import cn.edu.zjut.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {

    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 下订单时锁库存
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVO wareSkuLockVO) {
        try {
            this.wareSkuService.orderLockStock(wareSkuLockVO);
            return R.ok();
        } catch (NoStockException e) {
            return R.error(EmBizError.NO_STOCK_EXCEPTION);
        }
    }


    /**
     * 02、查询商品库存
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    // 查询 sku 是否有库存
    @PostMapping("/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockDTO> skuHasStockDTOs = this.wareSkuService.hasStock(skuIds);

        return R.ok().put("data", skuHasStockDTOs);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = this.wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        this.wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        this.wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
