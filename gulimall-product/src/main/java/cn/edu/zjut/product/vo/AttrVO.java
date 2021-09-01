package cn.edu.zjut.product.vo;

import cn.edu.zjut.product.entity.AttrEntity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttrVO extends AttrEntity {
    // 所属分组
    private Long attrGroupId;
}
