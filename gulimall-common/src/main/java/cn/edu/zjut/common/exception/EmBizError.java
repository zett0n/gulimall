package cn.edu.zjut.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用，001：参数格式校验    002：短信验证码频率太高
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 * 15: 用户
 * 21: 库存
 */
@AllArgsConstructor
@Getter
public enum EmBizError {
    /**
     * 系统未知异常
     */
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    RPC_EXCEPTION(10001, "远程调用异常"),
    /**
     * 参数校验错误
     */
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    SYSTEM_BUSY_EXCEPTION(10003, "系统繁忙，请稍后再试"),
    /**
     * Es检索商品异常
     */
    ES_CONNECTION_EXCEPTION(11000, "Es 连接异常"),
    PRODUCT_UP_EXCEPTION(11001, "商品上架异常"),
    /**
     * 用户注册注册异常
     */
    USERNAME_EXIST_EXCEPTION(15001, "用户名已存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "账号密码错误"),
    /**
     * 下单异常
     */
    NO_STOCK_EXCEPTION(21000, "商品库存不足"),
    ;

    private final int errCode;

    private final String errMsg;
}
