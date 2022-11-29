package com.stx.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.stx.reggie.common.BaseContext;
import com.stx.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 * @author Hasee
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public final static AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本此请求的URI
        String requestURI = request.getRequestURI();
        //定义不需要处理的URI
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        //2.判断是否需要处理
        boolean check = check(urls, requestURI);
        //3.如果不需要处理直接放行
        if (check){
            filterChain.doFilter(request,response);
            return;
        }
       //3.1判断登录状态，如果已经登陆直接放行
        if(null!=request.getSession().getAttribute("employee")){
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        //3.2判断登录状态，如果已经登陆直接放行
        if(null!=request.getSession().getAttribute("user")){
            Long userid = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userid);
            filterChain.doFilter(request,response);
            return;
        }
        //5.如果未登录，则返回未登录的结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配检查，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){

        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }


}
