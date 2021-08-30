package cn.edu.zjut.product.controller;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.entity.ProductAttrValueEntity;
import cn.edu.zjut.product.service.AttrService;
import cn.edu.zjut.product.service.ProductAttrValueService;
import cn.edu.zjut.product.vo.AttrRespVO;
import cn.edu.zjut.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    // 22、获取spu规格
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId) {

        List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueService.baseAttrListforspu(spuId);
        return R.ok().put("data", productAttrValueEntities);
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@PathVariable("attrType") String attrType,
                          @PathVariable("catelogId") Long catelogId,
                          @RequestParam Map<String, Object> params) {
        PageUtils page = this.attrService.queryBaseAttrPage(params, catelogId, attrType);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        // AttrEntity attr = this.attrService.getById(attrId);
        AttrRespVO attrRespVO = this.attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrRespVO);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody AttrVO attrVo) {
        this.attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody AttrVO attrVo) {
        this.attrService.updateAttr(attrVo);

        return R.ok();
    }

    // 23、修改商品规格
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> productAttrValueEntities) {
        this.productAttrValueService.updateSpuAttr(spuId, productAttrValueEntities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        this.attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
