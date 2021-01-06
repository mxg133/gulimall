package com.atguigu.gulimall.product.exception;

import com.atguigu.common.utils.R;
import com.atguigu.common.exception.BizCodeEnume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2020-12-22 12:20 下午
 * @description
 *  集中处理所有异常
 */

@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.atguigu.gulimall.product.app")
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.app")
public class GulimallExceptionControllerAdvice {

    //告诉mvc 这个方法能处理什么异常
    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });

        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data",errorMap);
    }

//    不行的话 来到这个最大的异常处理 能处理任意类型的异常
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){

        log.error("错误：",throwable);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
