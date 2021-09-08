package cn.edu.zjut.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ProductConstant {

    @AllArgsConstructor
    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性"),
        ;

        private final Integer val;
        private final String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum ProductStatusEnum {
        // TODO  数据库只有两个字段
        // publish_status tinyint null comment '上架状态[0 - 下架，1 - 上架]'

        NEW_SPU(0, "商品新建"),
        SPU_UP(1, "商品上架"),
        SPU_DOWN(2, "商品下架"),
        ;

        private final Integer val;
        private final String desc;
    }
}
