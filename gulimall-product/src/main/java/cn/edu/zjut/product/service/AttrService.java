package cn.edu.zjut.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.vo.AttrRespVO;
import cn.edu.zjut.product.vo.AttrVO;

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

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId);

    AttrRespVO getAttrInfo(Long attrId);
}
