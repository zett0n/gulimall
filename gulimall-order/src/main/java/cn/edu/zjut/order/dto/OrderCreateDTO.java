package cn.edu.zjut.order.dto;

import cn.edu.zjut.order.entity.OrderEntity;
import cn.edu.zjut.order.entity.OrderItemEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class OrderCreateDTO {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;

}
