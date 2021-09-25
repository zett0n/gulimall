package cn.edu.zjut.ware.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.FareVO;
import cn.edu.zjut.ware.entity.WareInfoEntity;
import cn.edu.zjut.ware.service.WareInfoService;

/**
 * 仓库信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {

    @Autowired
    private WareInfoService wareInfoService;

    /**
     * 获取运费信息
     */
    @GetMapping("/fare/{addrId}")
    FareVO getFare(@PathVariable("addrId") Long addrId) {
        return this.wareInfoService.getFare(addrId);
    }

    /**
     * 01、仓库列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareInfoEntity wareInfo = this.wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo) {
        this.wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo) {
        this.wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
