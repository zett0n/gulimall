package cn.edu.zjut.product.vo;

import cn.edu.zjut.product.entity.AttrEntity;
import cn.edu.zjut.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVO extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
