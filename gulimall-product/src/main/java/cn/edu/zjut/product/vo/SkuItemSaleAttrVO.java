package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString
@Accessors(chain = true)
public class SkuItemSaleAttrVO {

    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVO> attrValues;
}
