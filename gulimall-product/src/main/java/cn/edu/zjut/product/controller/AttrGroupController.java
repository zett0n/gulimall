package cn.edu.zjut.product.controller;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.entity.AttrGroupEntity;
import cn.edu.zjut.product.service.AttrAttrgroupRelationService;
import cn.edu.zjut.product.service.AttrGroupService;
import cn.edu.zjut.product.service.AttrService;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.AttrGroupRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 获取分类属性分组
     * catelogId 为路径变量
     */
    @GetMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = this.attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 获取属性分组详情
     */
    @GetMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = this.attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = this.categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        this.attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        this.attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        this.attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    // 获取分组相关联的所有属性
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrEntities = this.attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntities);
    }

    // 删除属性与分组的关系
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVO[] attrGroupRelationVOS) {
        this.attrService.deleteRelation(attrGroupRelationVOS);
        return R.ok();
    }

    // 获取属性分组没有关联的其他属性
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = this.attrService.getNoRelationAttr(attrgroupId, params);
        return R.ok().put("page", page);
    }

    // 添加属性与分组关联关系
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos) {

        this.relationService.saveBatch(vos);
        return R.ok();
    }
}
