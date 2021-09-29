package cn.edu.zjut.common.enume;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WareTaskStatusEnum {
    // TO_BE_LOCKED(0, "未锁定"),
    LOCKED(1, "已锁定"),
    UNLOCKED(2, "已解锁");
    // DOWN(3,"已扣减");

    private Integer code;
    private String msg;
}
