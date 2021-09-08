package cn.edu.zjut.product.service.impl;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.product.dao.AttrGroupDao;
import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.entity.AttrGroupEntity;
import cn.edu.zjut.product.service.AttrGroupService;
import cn.edu.zjut.product.service.AttrService;
import cn.edu.zjut.product.vo.AttrGroupWithAttrsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // sql优化？
        // select * from pms_attr_group where catelog_id=? and (atrr_group_id=key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();

        // 只有选三级分类，前端传非默认值的 catelogId 才将其纳入查询条件
        if (catelogId != DefaultConstant.ID_SELECT_ALL) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        // 如果 key 不为 null 和 ”“，需要根据关键字模糊查询
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper
                    .eq("attr_group_id", key)
                    .or()
                    .like("attr_group_name", key));
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper);
        return new PageUtils(page);
    }

    // 根据分类id查出所有的分组以及这些组里面的属性
    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        return attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVO attrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(group, attrsVO);
            List<AttrEntity> attrs = this.attrService.getRelationAttr(attrsVO.getAttrGroupId());
            attrsVO.setAttrs(attrs);
            return attrsVO;
        }).collect(Collectors.toList());
    }
}