package cn.edu.zjut.cart.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车全体商品信息
 * 需要计算的属性需要重写 get 方法，保证每次获取属性都会进行计算
 */
@Data
@Accessors(chain = true)
public class CartVO {
    /**
     * 购物车商品项信息
     */
    List<CartItemVO> items;

    /**
     * 已勾选的商品数量
     */
    private Integer countNum;

    /**
     * 已勾选的商品类型数量
     */
    private Integer countType;

    /**
     * 已勾选的商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    /**
     * 计算已勾选的商品数量
     */
    public Integer getCountNum() {
        int count = 0;
        for (CartItemVO item : this.items) {
            if (item.getCheck()) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 计算已勾选的商品种数
     */
    public Integer getCountType() {
        int count = 0;
        for (CartItemVO item : this.items) {
            if (item.getCheck()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 计算已勾选的商品总价
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 计算购物项总价
        if (!CollectionUtils.isEmpty(this.items)) {
            for (CartItemVO item : this.items) {
                if (item.getCheck()) {
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        // 计算优惠后的价格
        return amount.subtract(getReduce());
    }
}
