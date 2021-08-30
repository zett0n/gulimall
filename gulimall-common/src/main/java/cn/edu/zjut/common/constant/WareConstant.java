package cn.edu.zjut.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WareConstant {

    @AllArgsConstructor
    @Getter
    public enum PurchaseStatusEnum {
        CREATED(0, "采购单已新建"),
        ASSIGNED(1, "采购单已分配"),
        RECEIVE(2, "采购单已领取"),
        FINISH(3, "采购单已完成"),
        HAS_ERROR(4, "采购单采购失败");

        private final Integer val;
        private final String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum PurchaseDetailStatusEnum {
        CREATEED(0, "采购项已新建"),
        ASSIGNED(1, "采购项已分配"),
        BUYING(2, "采购项正在采购"),
        FINISH(3, "采购项已完成"),
        HAS_ERROR(4, "采购项采购失败");

        private final Integer val;
        private final String desc;
    }
}
