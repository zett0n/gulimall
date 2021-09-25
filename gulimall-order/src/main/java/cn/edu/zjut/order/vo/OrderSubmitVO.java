package cn.edu.zjut.order.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 订单提交信息
 * 
 * 无需提交要购买的商品，去购物车再获取一遍 用户相关的信息，直接去session中取出即可
 */
@Data
public class OrderSubmitVO {

    /** 收获地址的id **/
    private Long addrId;

    /** 支付方式 **/
    private Integer payType;

    // 优惠、发票

    /** 防重令牌 **/
    private String orderToken;

    /** 应付价格 **/
    private BigDecimal payPrice;

    /** 订单备注 **/
    private String remarks;

}
