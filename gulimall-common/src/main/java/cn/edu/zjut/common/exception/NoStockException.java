package cn.edu.zjut.common.exception;

import lombok.Data;

@Data
public class NoStockException extends RuntimeException {

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + " 库存不足");
    }

    public NoStockException(String message) {
        super(message);
    }
}
