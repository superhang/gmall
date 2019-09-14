package com.hangzhang.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.hangzhang.gmall.annotations.LoginRequired;
import com.hangzhang.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import utils.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            //拦截代码
            System.out.println("进入拦截");
            //判断被拦截的请求的访问的方法的注解  反射机制
            HandlerMethod hm = (HandlerMethod) handler;
            //获取请求是否含有自己标识的LoginRequired注解方法
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
            //是否需要拦截
            if(methodAnnotation==null){
                //不需要拦截
                return true;
            }
            /**
             * 购物车需要进行拦截器验证，但是即使没有登录也允许进行后续操作，需要通过注解的一个
             * 状态属性进行方法标识，区分一、二、三类方法
             * */
            //涉及矩阵问题
            String token = "";
            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
            String newtoken = request.getParameter("token");
            if(StringUtils.isNotBlank(oldToken)){
                token =oldToken;
            }
            if(StringUtils.isNotBlank(newtoken)){
                token=newtoken;
            }

            /**
             * 验证模块 认证中心
             * */
            String success = "fail";
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();//IP,没有Nginx的情况
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                    //做异常处理
                }
            }
            Map<String,String> successMap = new HashMap<>();
            if(StringUtils.isNotBlank(token)){
                String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8086/verify?token=" + token + "&currentIp="+ip);
                successMap = JSON.parseObject(successJson,Map.class);
                success = successMap.get("status");
            }


            //是否需要登录 注解实现（首页登录 主动登录）
            if(methodAnnotation.loginSuccess()){
                //必须登录成功才能使用
                if(!success.equals("success")){
                    //重定向回去登录
                    response.sendRedirect("http://passport.gmall.com:8086/index?ReturnUrl="+request.getRequestURL());
                    return false;
                }
                //验证通过，覆盖cookie中的token
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickname",successMap.get("nickname"));

                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }

            }else{
                //没有登录也能用，但是必须验证
                //验证通过
                if(success.equals("success")){
                    //需要将token携带的用户信息写入
                    request.setAttribute("memberId",successMap.get("memberId"));
                    request.setAttribute("nickname",successMap.get("nickname"));
                    //验证通过，覆盖cookie中的token
                    if(StringUtils.isNotBlank(token)){
                        CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                    }
                }
            }




            return true;
        }
}
