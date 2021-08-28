package cn.edu.zjut.product.vo;

import cn.edu.zjut.product.entity.AttrEntity;
import lombok.Data;

@Data
public class AttrVO extends AttrEntity {
    // 所属分组
    private Long attrGroupId;
}
