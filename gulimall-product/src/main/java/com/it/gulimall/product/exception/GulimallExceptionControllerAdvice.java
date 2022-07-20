package com.it.gulimall.product.exception;

import com.it.common.exception.BizCodeEnume;
import com.it.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理controller层的异常
 *
 * @author : code1997
 * @date : 2021/5/3 21:25
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.it.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    /**
     * 对于校验失败的异常处理
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现了异常：{" + e.getMessage() + "}" + "异常类型：{" + e.getClass() + "}");
        BindingResult result = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    /**
     * 对于其他异常的处理
     */
    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error("系统出现了异常：{" + e.getMessage() + "}" + "异常类型：{" + e.getClass() + "}");
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
