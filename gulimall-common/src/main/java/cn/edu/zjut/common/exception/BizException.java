package cn.edu.zjut.common.exception;

import lombok.Getter;

public class BizException extends RuntimeException {
    @Getter
    private EmBizError emBizError;

    public BizException(EmBizError emBizError) {
        this.emBizError = emBizError;
    }

}
