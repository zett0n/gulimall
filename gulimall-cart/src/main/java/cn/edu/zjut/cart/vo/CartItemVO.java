package cn.edu.zjut.cart.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车的商品项
 * 需要计算的属性需要重写 get 方法，保证每次获取属性都会进行计算
 */
@Data
@Accessors(chain = true)
public class CartItemVO {

    private Long skuId;

    /**
     * 是否选中
     */
    private Boolean check;

    private String title;

    private String image;

    /**
     * 商品套餐属性
     */
    private List<String> skuAttrValues;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    /**
     * 计算当前购物项总价
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count));
    }
}
