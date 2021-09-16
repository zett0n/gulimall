package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.vo.Catalog2VO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    // 找到catelog的完整路径
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getRootCategories();

    Map<String, List<Catalog2VO>> getCatalogJSONFromDB();

    Map<String, List<Catalog2VO>> getCatalogJSONFromRedis();

    Map<String, List<Catalog2VO>> getCatalogJSONFromRedisWithLocalLock();

    Map<String, List<Catalog2VO>> getCatalogJSONFromRedisWithRedisLock();

}
