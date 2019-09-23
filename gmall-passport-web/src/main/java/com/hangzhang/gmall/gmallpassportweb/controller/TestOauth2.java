package com.hangzhang.gmall.gmallpassportweb.controller;

import com.alibaba.fastjson.JSON;
import utils.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

    public static String getCode(){
        String YOUR_REGISTERED_REDIRECT_URI = "http://passport.gmall.com:8086/vlogin";
        String step1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=647656945&response_type=code&redirect_uri=http://passport.gmall.com:8086/vlogin");

        //有用户操作授权过程

        String step2 = "http://passport.gmall.com:8086/vlogin?code=4f51bcdb0b7e89c919c6fe9b7bacb69c";
        return step2;
    }

    public static String getAccess_token(String code){
        //必须是post请求，获取token
        // 例子 https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        String step3 = "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","647656945");
        paramMap.put("client_secret","235f6256e754a74b0e528e1cec9d3e50");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
        //授权码有限期可用，每新生成一次授权码，说明用户对第三方数据进行重启授权，只能用一次（用完就过期）
        paramMap.put("code",code);

        String access_token = HttpclientUtil.doPost(step3, paramMap);
        Map<String,String> map = JSON.parseObject(access_token, Map.class);
        //授权码 2.006x1VBH0lJVph3633275086JFV6MB
        return map.get("access_token");
    }

    public static Map<String,String> getUser_info(String access_token){
        //4 用access_token查询用户信息 微博API 可以查询到用户的相关信息
        String step4 = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid=1";
        String user_json = HttpclientUtil.doGet(step4);
        Map<String,String> user_map = JSON.parseObject(user_json, Map.class);
        return user_map;
    }

    public static void main(String args[]){
        //App Key：647656945
        //App Secret：235f6256e754a74b0e528e1cec9d3e50
        String YOUR_CLIENT_ID = "647656945";
        //1

        //2
        //使用授权码
        //http://passport.gmall.com:8086/vlogin?code=4f51bcdb0b7e89c919c6fe9b7bacb69c

        //3




    }
}
