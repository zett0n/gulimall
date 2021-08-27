package cn.edu.zjut.product.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.CategoryDao;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.CategoryService;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page =
            this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查出所有分类
        List<CategoryEntity> entities = this.baseMapper.selectList(null);

        // 2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类，给children设置子分类
        return entities.stream()
            // 过滤找出一级分类
            .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
            // 处理，给一级菜单递归设置子菜单
            .peek(menu -> menu.setChildren(getChildless(menu, entities)))
            // 按sort属性排序
            .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
            .collect(Collectors.toList());
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildless(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
            .peek(categoryEntity -> {
                // 找到子菜单
                categoryEntity.setChildren(getChildless(categoryEntity, all));
            }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
            .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查要删除的菜单是否被别的地方引用
        this.baseMapper.deleteBatchIds(asList);
    }
}