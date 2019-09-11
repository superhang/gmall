package com.hangzhang.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT的测试例子
 * */
public class TestJwt {
    public static void main(String args[]){
        //加密
        HashMap<String, Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickname","zhangshan");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date());
        String encode = JwtUtil.encode("2019gmall0105", map, ip + time);
        System.out.println(encode);
        //解密
        Map<String, Object> decodeMap = new HashMap<>();
        decodeMap = JwtUtil.decode(encode,"2019gmall0105",ip + time);
        for(String key:decodeMap.keySet()){
            System.out.println(key+":  "+decodeMap.get(key));
        }

    }
}
