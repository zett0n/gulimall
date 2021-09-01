package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttrRespVO extends AttrVO {
    // 所属分类名字（手机/数码/手机）
    private String catelogName;

    // 所属分组名字（主体）
    private String attrGroupName;

    // 目录完整路径
    private Long[] catelogPath;
}
