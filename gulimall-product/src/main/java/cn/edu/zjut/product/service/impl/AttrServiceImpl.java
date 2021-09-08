package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.ProductConstant;
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
import cn.edu.zjut.product.vo.AttrGroupRelationVO;
import cn.edu.zjut.product.vo.AttrRespVO;
import cn.edu.zjut.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(attrType) ? 1 : 0);

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVO> respVOList = records.stream().map(attrEntity -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRespVO);

            // 销售属性无需设置分组
            if ("base".equalsIgnoreCase(attrType)) {
                // 设置分组名（这里没有用联表查询，而是分两次单表查询）
                // 1. 根据 attrEntity 查 attr_id 在 pms_attr_attrgroup_relation 表中对应的记录
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = this.attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));

                // 2. 如果查得到记录，根据记录获得 attr_group_id 到 pms_attr_group 表中查到分组名
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                }
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

    @Transactional
    @Override
    public void saveAttr(AttrVO attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);

        // 保存基本数据
        this.save(attrEntity);

        // 保存关联关系（销售属性不需要）
        if (Objects.equals(attrVo.getAttrType(), ProductConstant.AttrEnum.ATTR_TYPE_BASE.getVal())
                && attrVo.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();

            // 保存基本数据时 attrEntity 的 attrId 更新了
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId())
                    .setAttrId(attrEntity.getAttrId());

            this.attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrRespVO attrRespVO = new AttrRespVO();
        AttrEntity attrEntity = getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVO);

        if (Objects.equals(attrEntity.getAttrType(), ProductConstant.AttrEnum.ATTR_TYPE_BASE.getVal())) {
            // 设置分组信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = this.attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));

            if (attrAttrgroupRelationEntity != null) {
                attrRespVO.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());

                AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(
                        attrAttrgroupRelationEntity.getAttrGroupId());

                if (attrGroupEntity != null) {
                    attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                }
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

    @Override
    @Transactional
    public void updateAttr(AttrVO attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        if (Objects.equals(attrEntity.getAttrType(), ProductConstant.AttrEnum.ATTR_TYPE_BASE.getVal())) {
            // 修改或新增分组关联
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId())
                    .setAttrId(attrVo.getAttrId());

            Integer count = this.attrAttrgroupRelationDao.selectCount(
                    new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId())
            );
            if (count > 0) {
                this.attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                this.attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    // 根据分组id查找关联的所有属性
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = this.attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        if (attrAttrgroupRelationEntities.isEmpty()) {
            return null;
        }

        List<Long> attrIds = attrAttrgroupRelationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        return this.listByIds(attrIds);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] attrGroupRelationVOS) {
        // this.attrAttrgroupRelationDao.delete(new QueryWrapper<>().eq("attr_id", ).eq("attr_group_id"))

        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.stream(attrGroupRelationVOS).
                map(attrGroupRelationVO -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrGroupRelationVO, attrAttrgroupRelationEntity);

                    return attrAttrgroupRelationEntity;
                })
                .collect(Collectors.toList());

        this.attrAttrgroupRelationDao.deleteBatchRelation(attrAttrgroupRelationEntities);
    }

    // 获取属性分组没有关联的其他属性
    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        // 当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 当前分组只能关联别的分组没有引用的属性
        // 找出当前分类下的其他分组
        List<AttrGroupEntity> groups = this.attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<Long> groupIds = groups.stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        // 找出这些分组相关联的属性
        List<AttrAttrgroupRelationEntity> relations = this.attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));

        List<Long> attrIds = relations.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        // 从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getVal());

        if (!attrIds.isEmpty()) {
            wrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and((w) -> w.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return this.baseMapper.selectSearchAttrIds(attrIds);
    }
}