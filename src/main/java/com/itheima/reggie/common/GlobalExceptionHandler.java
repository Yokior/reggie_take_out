package com.itheima.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

// 全局异常处理
@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler
{
    /**
     * 处理数据库异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex)
    {
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry"))
        {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在！";
            return R.error(msg);
        }

        return R.error("网络繁忙 请稍后再试");
    }


    /**
     * 处理用户异常
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex)
    {
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
