package cn.edu.zjut.common.dto.mq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class StockLockDTO {

    // 订单锁定工作单的 id
    private Long taskId;

    // 库存锁定工作单的 id
    private List<Long> detailIds = new ArrayList<>();

    // private StockLockDetailDTO stockLockDetailDTO;

}
