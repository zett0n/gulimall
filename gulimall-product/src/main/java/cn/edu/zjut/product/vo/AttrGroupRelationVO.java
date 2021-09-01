package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttrGroupRelationVO {
    private Long attrId;
    private Long attrGroupId;
}
