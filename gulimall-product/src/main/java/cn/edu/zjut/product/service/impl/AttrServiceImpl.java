package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.AttrAttrgroupRelationDao;
import cn.edu.zjut.product.dao.AttrDao;
import cn.edu.zjut.product.dao.AttrGroupDao;
import cn.edu.zjut.product.dao.CategoryDao;
import cn.edu.zjut.product.entity.AttrAttrgroupRelationEntity;
import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.entity.AttrGroupEntity;
import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.AttrService;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.AttrRespVO;
import cn.edu.zjut.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        // TODO stream map
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVO> respVOList = records.stream().map(attrEntity -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRespVO);

            // 设置分组名（这里没有用联表查询，而是分两次单表查询）
            // 1. 根据 attrEntity 查 attr_id 在 pms_attr_attrgroup_relation 表中对应的记录
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = this.attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));

            // 2. 如果查得到记录，根据记录获得 attr_group_id 到 pms_attr_group 表中查到分组名
            if (attrAttrgroupRelationEntity != null) {
                AttrGroupEntity attrGroupEntity =
                        this.attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
            }

            // 设置分类名
            CategoryEntity categoryEntity = this.categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVO.setCatelogName(categoryEntity.getName());
            }

            return attrRespVO;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVOList);
        return pageUtils;
    }

    @Override
    @Transactional
    public void saveAttr(AttrVO attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);

        // 保存基本数据
        this.save(attrEntity);

        // 保存关联关系
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        // 保存基本数据时 attrEntity 的 attrId 更新了
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        this.attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrRespVO attrRespVO = new AttrRespVO();
        AttrEntity attrEntity = getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVO);

        // 设置分组信息
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = this.attrAttrgroupRelationDao
                .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));

        if (attrAttrgroupRelationEntity != null) {
            attrRespVO.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
            if (attrGroupEntity != null) {
                attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        // 设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = this.categoryService.findCatelogPath(catelogId);
        attrRespVO.setCatelogPath(catelogPath);

        CategoryEntity categoryEntity = this.categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            attrRespVO.setCatelogName(categoryEntity.getName());
        }

        return attrRespVO;
    }
}