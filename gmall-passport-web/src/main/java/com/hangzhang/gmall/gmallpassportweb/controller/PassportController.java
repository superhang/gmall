package com.hangzhang.gmall.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.beans.UmsMember;
import com.hangzhang.gmall.util.JwtUtil;
import com.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){
        if(StringUtils.isNotBlank(ReturnUrl)){
            map.put("ReturnUrl",ReturnUrl);
        }
        return "index";
    }
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember,HttpServletRequest request){
        UmsMember umsMemberlogin = userService.login(umsMember);
        String token = "";
        if(umsMemberlogin!=null){
            //登录成功

            //用jwt制作token
            String id = umsMemberlogin.getId();
            String nickname = umsMemberlogin.getNickname();
            Map<String,Object> userMap = new HashMap<>();
            userMap.put("memberId",id);
            userMap.put("nickname",nickname);
            //这个时候获取的request是应用服务的地址（经过拦截的）
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();//IP,没有Nginx的情况
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                    //做异常处理
                }
            }

            //按照设计的算法进行加密生成token
            token = JwtUtil.encode("2019gmall8888", userMap, ip);


            //将token存入redis一份
            userService.addUserToken(token,id);
        }else{
            //登录失败
            return "fail";
        }
        return token;
    }

    @RequestMapping("verify")
    @ResponseBody
    //当前ip
    public String verify(String token,String currentIp){
        //通过jwt校验token的真假
        Map<String,String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall8888", currentIp);
        if(decode!=null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickname",(String)decode.get("nickname"));
        }


        return JSON.toJSONString(map);
    }
}
