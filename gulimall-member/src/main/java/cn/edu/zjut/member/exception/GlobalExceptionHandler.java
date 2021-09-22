package cn.edu.zjut.member.exception;

import cn.edu.zjut.common.exception.BizException;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice(basePackages = "cn.edu.zjut.member.api")
public class GlobalExceptionHandler {

    /**
     * 自定义业务异常处理
     */
    @ExceptionHandler(BizException.class)
    public R handleBizException(BizException e) {
        log.error("自定义业务异常：{}，异常类型：{}", e.getMessage(), e.getClass());

        EmBizError emBizError = e.getEmBizError();
        log.error("errCode：{}，errMsg：{}", emBizError.getErrCode(), emBizError.getErrMsg());

        return R.error(emBizError);
    }

    /**
     * JSR303 数据校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("出现数据校验问题：{}，异常类型：{}", e.getMessage(), e.getClass());

        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> errMap.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return R.error(EmBizError.VALID_EXCEPTION).put("data", errMap);
    }

    /**
     * 其他异常处理
     */
    @ExceptionHandler(Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("出现未知异常：{}，异常类型：{}", throwable.getMessage(), throwable.getClass());

        return R.error(EmBizError.UNKNOWN_EXCEPTION);
    }
}
