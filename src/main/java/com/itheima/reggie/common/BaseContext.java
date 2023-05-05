package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类 用户保存和获取当前用户登录ID
 * 每一次线程的变量
 */
public class BaseContext
{
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 保存用户ID
     * @param id
     */
    public static void setCurrentId(Long id)
    {
        threadLocal.set(id);
    }

    /**
     * 获取用户ID
     * @return
     */
    public static Long getCurrentId()
    {
        return threadLocal.get();
    }
}
