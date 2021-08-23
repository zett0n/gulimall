package cn.edu.zjut.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:14:57
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

