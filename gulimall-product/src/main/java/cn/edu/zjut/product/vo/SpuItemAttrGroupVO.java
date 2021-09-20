package cn.edu.zjut.product.vo;


import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString
@Accessors(chain = true)
public class SpuItemAttrGroupVO {

    private String groupName;

    private List<Attr> attrs;

}
