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
import utils.HttpclientUtil;

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
    @RequestMapping("vlogin")
    //当前ip
    public String vlogin(String code,HttpServletRequest request){
        //授权码换取access_token
        String access_token_url = "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","647656945");
        paramMap.put("client_secret","235f6256e754a74b0e528e1cec9d3e50");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
        //授权码有限期可用，每新生成一次授权码，说明用户对第三方数据进行重启授权，只能用一次（用完就过期）
        paramMap.put("code",code);
        String access_token_json = HttpclientUtil.doPost(access_token_url, paramMap);
        Map<String,Object> map = JSON.parseObject(access_token_json, Map.class);

        //access_token换取用户信息
        String uid = (String) map.get("uid");
        String access_token = (String)map.get("access_token");

        String show_user_url = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String,Object> user_map = JSON.parseObject(user_json, Map.class);

        //将用户信息保存到数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();

        umsMember.setSourceType("2");
        umsMember.setAccess_code(code);
        umsMember.setAccess_token(access_token);
        umsMember.setSource_uid(String.valueOf(user_map.get("id")));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String)user_map.get("screen_name"));

        //检查这个用户是否存在
        UmsMember usercheck = new UmsMember();
        usercheck.setSource_uid(umsMember.getSource_uid());
        UmsMember uc = userService.checkOauthUser(usercheck);//检查之前是否登录

        if(uc==null){
            umsMember = userService.addOauthUser(umsMember);//主键返回策略不能跨越rpc
        }else{
            umsMember = uc;
        }

        //生成jwt的token信息，重定向到首页，携带该token
        String token = "";
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();

        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
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
        userService.addUserToken(token,memberId);
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

}
