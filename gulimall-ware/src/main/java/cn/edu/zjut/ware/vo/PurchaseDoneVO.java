package cn.edu.zjut.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class PurchaseDoneVO {
    // 采购单id
    @NotNull
    private Long id;

    private List<PurchaseItemDoneVO> items;
}