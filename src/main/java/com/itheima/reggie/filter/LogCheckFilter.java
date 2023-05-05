package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 */
@Slf4j
@WebFilter(filterName = "logCheckFilter", urlPatterns = "/*")
public class LogCheckFilter implements Filter
{
    // 路径匹配器 支持通配符匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

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
                        "/front/**",
                        "/common/**",
                        "/user/sendMsg",
                        "/user/login"
                };
        for (String url : urls)
        {
            boolean match = PATH_MATCHER.match(url, requestURI);
            // 是可供放行的请求则直接放行
            if (match)
            {
                log.info("本次请求可直接放行 {}",requestURI);
                filterChain.doFilter(request, response);
                return;
            }
        }
        // 1.判断登录状态 如果已登录 则放行
        if (request.getSession().getAttribute("employee") != null)
        {
            log.info("用户已经登录 id为{}",request.getSession().getAttribute("employee"));

            // 将用户ID保存到ThreadLocal备用
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            long id = Thread.currentThread().getId();
            log.info("线程ID为:{}",id);

            filterChain.doFilter(request, response);
            return;
        }

        // 2.判断移动端登录状态 如果已登录 则放行
        if (request.getSession().getAttribute("user") != null)
        {
            log.info("移动端用户已经登录 id为{}",request.getSession().getAttribute("user"));

            // 将用户ID保存到ThreadLocal备用
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            long id = Thread.currentThread().getId();
            log.info("线程ID为:{}",id);

            filterChain.doFilter(request, response);
            return;
        }


        // 没有登录 通过输出流方式写数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
}
