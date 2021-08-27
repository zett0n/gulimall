package cn.edu.zjut.product.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import lombok.extern.slf4j.Slf4j;

// 全局异常处理
@Slf4j
@RestControllerAdvice(basePackages = "cn.edu.zjut.product.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("出现数据校验问题：{}，异常类型：{}", e.getMessage(), e.getClass());

        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errMap = new HashMap<>();
        bindingResult.getFieldErrors()
            .forEach(fieldError -> errMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return R.error(EmBizError.VALID_EXCEPTION).put("data", errMap);
    }

    @ExceptionHandler(Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("出现未知异常：{}，异常类型：{}", throwable.getMessage(), throwable.getClass());

        return R.error(EmBizError.UNKNOWN_EXCEPTION);
    }
}
