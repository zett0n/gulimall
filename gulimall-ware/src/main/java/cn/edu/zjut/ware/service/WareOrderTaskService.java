package cn.edu.zjut.ware.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.ware.entity.WareOrderTaskEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getByOrderSn(String orderSn);
}

