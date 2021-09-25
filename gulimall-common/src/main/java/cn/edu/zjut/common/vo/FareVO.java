package cn.edu.zjut.common.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FareVO {

    private MemberAddressVO address;

    private BigDecimal fare;
}
