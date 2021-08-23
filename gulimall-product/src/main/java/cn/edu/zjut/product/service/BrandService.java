package cn.edu.zjut.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.common.utils.PageUtils;
import cn.edu.zjut.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

