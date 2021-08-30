package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.ProductAttrValueDao;
import cn.edu.zjut.product.entity.ProductAttrValueEntity;
import cn.edu.zjut.product.service.ProductAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    // TODO @Transactional?
    @Transactional
    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        this.saveBatch(collect);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListforspu(Long spuId) {
        return this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }
    
    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities) {
        // 1、删除这个 spuId 之前对应的所有属性
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        // 2、删除这个 spuId 之前对应的所有属性
        List<ProductAttrValueEntity> collect = productAttrValueEntities.stream().peek(
                item -> item.setSpuId(spuId)).collect(Collectors.toList()
        );
        this.saveBatch(collect);
    }

}