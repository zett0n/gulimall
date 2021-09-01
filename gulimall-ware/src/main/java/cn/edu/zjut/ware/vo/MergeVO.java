package cn.edu.zjut.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MergeVO {
    // 整单id
    private Long purchaseId;

    // [1,2,3,4] 合并项集合
    private List<Long> items;
}