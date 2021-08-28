package cn.edu.zjut.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ProductConstant {
    @AllArgsConstructor
    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private final int attrTypeCode;
        private final String attrTypeMsg;
    }

}
