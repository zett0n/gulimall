package cn.edu.zjut.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WareSkuLockVO {

    private String OrderSn;

    private List<OrderItemVO> locks;

}
