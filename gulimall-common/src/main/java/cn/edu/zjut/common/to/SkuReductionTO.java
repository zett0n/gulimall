package cn.edu.zjut.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuReductionTO {

    private Long skuId;

    // 满几件打几折
    private int fullCount;
    private BigDecimal discount;
    
    // 满减
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;

    private int countStatus;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
