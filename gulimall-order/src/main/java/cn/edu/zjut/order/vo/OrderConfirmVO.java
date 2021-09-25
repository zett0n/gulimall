package cn.edu.zjut.order.vo;

import cn.edu.zjut.common.vo.MemberAddressVO;
import cn.edu.zjut.common.vo.OrderItemVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要的数据
 */
@Data
public class OrderConfirmVO {
    /**
     * 收获地址 ums_member_receive_address
     */
    private List<MemberAddressVO> memberAddressVOs;
    /**
     * 所有选中的购物项
     */
    private List<OrderItemVO> items;
    /**
     * 优惠券（会员积分）
     */
    private Integer integration;
    /**
     * 防止重复提交的令牌
     */
    private String orderToken;
    /**
     * 描述每个商品是否有库存
     */
    Map<Long, Boolean> stocks;

    /**
     * 计算订单总额
     */
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (this.items != null && this.items.size() > 0) {
            for (OrderItemVO item : this.items) {
                // 计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                // 再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }

    /**
     * 计算全部商品总件数
     */
    public Integer getCount() {
        Integer count = 0;
        if (this.items != null) {
            for (OrderItemVO item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 应付价格
     */
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
