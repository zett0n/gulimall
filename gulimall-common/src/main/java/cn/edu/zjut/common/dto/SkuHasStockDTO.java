package cn.edu.zjut.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuHasStockDTO {
    private Long skuId;
    private Boolean hasStock;
}
