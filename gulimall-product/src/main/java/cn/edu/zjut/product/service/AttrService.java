package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.vo.AttrGroupRelationVO;
import cn.edu.zjut.product.vo.AttrRespVO;
import cn.edu.zjut.product.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO attrVo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVO getAttrInfo(Long attrId);

    void updateAttr(AttrVO attrVo);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVO[] attrGroupRelationVOS);

    PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params);

    /**
     * 在指定的所有属性集合里，挑出可被检索的属性
     *
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}
