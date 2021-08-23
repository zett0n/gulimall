package cn.edu.zjut.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.common.utils.PageUtils;
import cn.edu.zjut.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-23 23:20:45
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

