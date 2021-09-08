package cn.edu.zjut.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

// TODO 没用的VO
@Data
@Accessors(chain = true)
public class SkuHasStockVO {
    private Long skuId;
    private Boolean hasStock;
}
