package com.hangzhang.gmall.gmallmanageservice.redissonTest;

import com.hangzhang.gmall.util.RedisUtil;
import jodd.util.StringUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissonController {
    //redisson测试 高并发测试
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @RequestMapping("lockTest")
    @ResponseBody
    public String lockTest(){
        Jedis jedis = redisUtil.getJedis();// redis链接
        RLock lock = redissonClient.getLock("redis-lock");//分布锁
        //加锁
        lock.lock();
        try {
            String v = jedis.get("k");//获取value
            System.err.println("==>"+v);//打印value
            if(StringUtil.isBlank(v)){
                v = "1";
            }
            int inum = Integer.parseInt(v);//获得value的值
            jedis.set("k", inum+1+"");//value增加1

        } finally {
            jedis.close();
            lock.unlock();
        }
        return "success";
    }
}
