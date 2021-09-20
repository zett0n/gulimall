package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttrValueWithSkuIdVO {

    private String attrValue;

    private String skuIds;

}
