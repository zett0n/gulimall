package cn.edu.zjut.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码和错误信息定义类
 * <p>
 * 1. 错误码定义规则为5为数字
 * <p>
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * <p>
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * <p>
 * 错误码列表：
 * <p>
 * 10: 通用，001：参数格式校验
 * <p>
 * 11: 商品
 * <p>
 * 12: 订单
 * <p>
 * 13: 购物车
 * <p>
 * 14: 物流
 */
@AllArgsConstructor
@Getter
public enum EmBizError {
    /**
     * 系统未知异常
     */
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    /**
     * 参数校验错误
     */
    VALID_EXCEPTION(10001, "参数格式校验失败");

    private final int errCode;
    private final String errMsg;
}
