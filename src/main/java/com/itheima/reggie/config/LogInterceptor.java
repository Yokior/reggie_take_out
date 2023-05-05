package com.itheima.reggie.config;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 自己写的拦截器

@Slf4j
//@Component
public class LogInterceptor implements HandlerInterceptor
{
    @Autowired
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        // 获取本次请求url
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        // 判断本次请求是否需要处理
        String urls[] = new String[]
                {
                        // 可供放行的请求
                        "/employee/login",
                        "/employee/logout",
                        "/backend/**",
                        "/front/**"
                };
        for (String url : urls)
        {
            boolean match = PATH_MATCHER.match(url, requestURI);
            // 是可供放行的请求则直接放行
            if (match)
            {
                log.info("本次请求可直接放行 {}",requestURI);
                return true;
            }
        }
        // 判断登录状态 如果已登录 则放行
        if (request.getSession().getAttribute("employee") != null)
        {
            log.info("用户已经登录 id为{}",request.getSession().getAttribute("employee"));
            return true;
        }
        // 没有登录 通过输出流方式写数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return false;
    }
}
