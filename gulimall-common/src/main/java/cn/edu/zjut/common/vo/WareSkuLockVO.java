package cn.edu.zjut.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVO {

    private String OrderSn;

    private List<OrderItemVO> locks;

}
