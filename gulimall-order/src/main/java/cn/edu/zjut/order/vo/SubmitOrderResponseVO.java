package cn.edu.zjut.order.vo;

import cn.edu.zjut.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVO {

    private OrderEntity order;

    /** 错误状态码 **/
    private Integer code;
}
