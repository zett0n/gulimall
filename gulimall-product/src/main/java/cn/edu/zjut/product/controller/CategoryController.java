package cn.edu.zjut.product.controller;

import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 商品三级分类
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 01、查出所有分类以及子分类，以树形结构组装起来
     */
    @GetMapping("/list/tree")
    public R list() {
        List<CategoryEntity> categoryEntities = this.categoryService.listWithTree();

        return R.ok().put("data", categoryEntities);
    }

    /**
     * 02、修改分类父子关系以及顺序
     */
    @PostMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category) {
        this.categoryService.updateBatchById(Arrays.asList(category));
        
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = this.categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category) {
        this.categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category) {
        this.categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] catIds) {
        // this.categoryService.removeByIds(Arrays.asList(catIds));

        // 检查要删除的菜单是否被别的地方引用
        this.categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
