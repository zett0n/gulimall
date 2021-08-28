package cn.edu.zjut.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.product.entity.BrandEntity;

/**
 * 品牌
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

