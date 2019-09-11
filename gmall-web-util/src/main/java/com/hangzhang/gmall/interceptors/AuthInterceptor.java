package com.hangzhang.gmall.interceptors;

import com.hangzhang.gmall.annotations.LoginRequired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//            String newToken = request.getParameter("newToken");
//            if(newToken!=null&&newToken.length()>0){
//                CookieUtil.setCookie(request,response,"token",newToken,WebConst.cookieExpire,false);
//            }
            //拦截代码
            System.out.println("进入拦截");
            //判断被拦截的请求的访问的方法的注解  反射机制
            HandlerMethod hm = (HandlerMethod) handler;
            //获取请求是否含有自己标识的LoginRequired注解方法
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
            if(methodAnnotation==null){
                return true;
            }
            /**
             * 购物车需要进行拦截器验证，但是即使没有登录也允许进行后续操作，需要通过注解的一个
             * 状态属性进行方法标识，区分一、二、三类方法
             * */
            if(methodAnnotation.loginSuccess()){

            }

            return true;
        }
}
