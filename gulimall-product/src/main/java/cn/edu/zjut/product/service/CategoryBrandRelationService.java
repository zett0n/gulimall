package cn.edu.zjut.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.CategoryBrandRelationEntity;

/**
 * 品牌分类关联
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);
}

