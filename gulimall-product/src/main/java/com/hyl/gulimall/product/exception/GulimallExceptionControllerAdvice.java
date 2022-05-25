package com.hyl.gulimall.product.exception;

import com.hyl.common.exception.BizCodeEnume;
import com.hyl.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hyl_marco
 * @data 2022/3/17 - 12:24
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.hyl.gulimall.product.controller")

@RestControllerAdvice(basePackages = "com.hyl.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());

        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((error)->{
            errorMap.put(error.getField(),error.getDefaultMessage());
        });

        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(),BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data",errorMap);
    }

    @ExceptionHandler
    public R handleException(Exception e){
        System.out.println(e.getMessage()+ e.getClass());
        log.error(e.getMessage(),e.getClass());
        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(),BizCodeEnume.UNKNOWN_EXCEPTION.getMsg());
    }

    @ExceptionHandler
    public R handleThrowable(Throwable throwable){
        log.error(throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(),BizCodeEnume.UNKNOWN_EXCEPTION.getMsg());
    }

}
