package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BrandVO {
    private Long brandId;
    private String brandName;
}
