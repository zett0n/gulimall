package cn.edu.zjut.product.controller;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.entity.SpuInfoEntity;
import cn.edu.zjut.product.service.SpuInfoService;
import cn.edu.zjut.product.vo.SpuSaveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * spu信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {

    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 18、spu检索
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }

    /**
     * 19、新增商品
     */
    @PostMapping("/save")
    public R save(@RequestBody SpuSaveVO spuSaveVO) {
        this.spuInfoService.saveSpuInfo(spuSaveVO);
        return R.ok();
    }

    /**
     * 20、商品上架
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        this.spuInfoService.up(spuId);
        return R.ok();
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = this.spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        this.spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
