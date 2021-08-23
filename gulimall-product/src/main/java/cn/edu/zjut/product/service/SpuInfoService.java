package cn.edu.zjut.product.service;

import cn.edu.zjut.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

