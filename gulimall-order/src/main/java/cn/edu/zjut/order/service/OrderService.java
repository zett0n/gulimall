package cn.edu.zjut.order.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.order.entity.OrderEntity;
import cn.edu.zjut.order.vo.OrderConfirmVO;
import cn.edu.zjut.order.vo.OrderSubmitVO;
import cn.edu.zjut.order.vo.SubmitOrderResponseVO;

/**
 * 订单
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:14:57
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO);
}
