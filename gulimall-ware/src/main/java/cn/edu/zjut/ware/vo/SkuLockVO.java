package cn.edu.zjut.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SkuLockVO {

    private Long skuId;

    private Integer skuNum;

    private List<Long> wareIds;

}
