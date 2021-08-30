package cn.edu.zjut.ware.vo;

import lombok.Data;

// TODO 两个相关联的VO使用内部类？
@Data
public class PurchaseItemDoneVO {
    // {itemId:1,status:4,reason:""}

    private Long itemId;

    private Integer status;

    private String reason;
}
