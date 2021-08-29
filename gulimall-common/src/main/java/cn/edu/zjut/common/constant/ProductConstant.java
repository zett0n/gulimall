package cn.edu.zjut.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ProductConstant {
    @AllArgsConstructor
    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private final Integer val;
        private final String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum SkuEnum {
        SKU_DEFAULT_IMG(1, "sku默认图片");

        private final Integer val;
        private final String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum REnum {
        R_SUCCESS_CODE(0, "success R code");

        private final Integer val;
        private final String desc;
    }

}
