package cn.edu.zjut.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class SpuBoundDTO {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
