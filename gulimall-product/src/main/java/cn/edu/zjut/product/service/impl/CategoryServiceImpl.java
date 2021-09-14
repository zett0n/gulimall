package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.CategoryDao;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.CategoryBrandRelationService;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.Catalog2VO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // TODO stream api 复习
        // 1、查出所有分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 2、组装成父子的树形结构
        return categoryEntities.stream()
                // 过滤找出一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == DefaultConstant.NO_PARENT_CATEGORY)
                // 处理，给一级菜单递归设置子菜单
                .peek(menu -> menu.setChildren(findChildren(menu, categoryEntities)))
                // 因为按sort属性排序，所以 menu-> .. 将需要排序的字段 sort 映射给 Comparator 排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? DefaultConstant.SORT_DEFAULT_VALUE : menu.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> findChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek(categoryEntity -> categoryEntity.setChildren(findChildren(categoryEntity, all)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? DefaultConstant.SORT_DEFAULT_VALUE : menu.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查要删除的菜单是否被别的地方引用
        this.baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> catelogPath = findParentPath(catelogId, new ArrayList<>());
        Collections.reverse(catelogPath);
        return catelogPath.toArray(new Long[0]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);

        Long parentCid = this.getById(catelogId).getParentCid();
        if (parentCid != DefaultConstant.NO_PARENT_CATEGORY) {
            findParentPath(parentCid, path);
        }
        return path;
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        // 更新时需要同步更新 pms_category_brand_relation 等表中的冗余信息
        if (StringUtils.isNotEmpty(category.getName())) {
            this.categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO 更新其他关联
        }
    }

    /**
     * 查询一级分类
     */
    @Override
    public List<CategoryEntity> getRootCategories() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1L));
    }

    /**
     * 查询所有目录，组装为 JSON
     * 只需查一次数据库，后续通过 stream 封装
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJSON() {
        // 查询目录所有信息
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 获取一级分类
        List<CategoryEntity> level1Categories = filterByParentCid(categoryEntities, 0L);

        // map 用于封装返回结果
        Map<String, List<Catalog2VO>> map = new HashMap<>();

        level1Categories.forEach(l1 -> {
            List<Catalog2VO> catalog2VOs = null;

            // 对于每个一级分类，查询它的二级分类
            List<CategoryEntity> level2Categories = filterByParentCid(categoryEntities, l1.getCatId());

            if (!level2Categories.isEmpty()) {
                catalog2VOs = level2Categories.stream()
                        .map(l2 -> {
                            Catalog2VO catalog2VO = new Catalog2VO();
                            catalog2VO.setCatalog1Id(l1.getCatId().toString())
                                    .setId(l2.getCatId().toString())
                                    .setName(l2.getName());

                            List<Catalog2VO.Catalog3VO> catalog3VOs = null;

                            // 对于每个二级分类，查询它的三级分类
                            List<CategoryEntity> level3Categories = filterByParentCid(categoryEntities, l2.getCatId());
                            if (!level3Categories.isEmpty()) {
                                catalog3VOs = level3Categories.stream()
                                        .map(l3 -> {
                                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO();
                                            catalog3VO.setCatalog2Id(l2.getCatId().toString())
                                                    .setId(l3.getCatId().toString())
                                                    .setName(l3.getName());

                                            return catalog3VO;
                                        })
                                        .collect(Collectors.toList());
                            }
                            catalog2VO.setCatalog3List(catalog3VOs);
                            return catalog2VO;
                        })
                        .collect(Collectors.toList());
            }
            map.put(l1.getCatId().toString(), catalog2VOs);
        });
        return map;
    }

    /**
     * 按父分类 id 过滤出对应的子分类
     */
    private List<CategoryEntity> filterByParentCid(List<CategoryEntity> categoryEntities, Long parentCid) {
        return categoryEntities.stream()
                .filter(item -> Objects.equals(item.getParentCid(), parentCid))
                .collect(Collectors.toList());
    }
}