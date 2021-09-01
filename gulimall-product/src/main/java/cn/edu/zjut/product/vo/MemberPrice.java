/**
 * Copyright 2019 bejson.com
 */
package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@Accessors(chain = true)
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}
