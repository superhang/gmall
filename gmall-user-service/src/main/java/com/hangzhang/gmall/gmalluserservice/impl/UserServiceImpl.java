package com.hangzhang.gmall.gmalluserservice.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.beans.UmsMember;
import com.beans.UmsMemberReceiveAddress;
import com.hangzhang.gmall.gmalluserservice.mapper.UmsMemberReceiveAddressMapper;
import com.hangzhang.gmall.gmalluserservice.mapper.UserMapper;
import com.hangzhang.gmall.util.RedisUtil;
import com.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
//使用rpc，dubbo扫描
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public boolean selectUser() {
        return false;
    }

    @Override
    public List<UmsMember> getAllUser() {
//        List<UmsMember> umsMemberList= userMapper.selectAllUser();
        List<UmsMember> umsMemberList= userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String menberId) {
        //way1
//        UmsMemberReceiveAddress UmsMemberReceiveAddress = new UmsMemberReceiveAddress();
//        UmsMemberReceiveAddress.setMemberId(menberId);
//        List<com.hangzhang.gmall.gmalluser.bean.UmsMemberReceiveAddress> select = umsMemberReceiveAddressMapper.select(UmsMemberReceiveAddress);
        //way2
        Example e = new Example(UmsMemberReceiveAddress.class);
        e.createCriteria().andEqualTo( "memberId",menberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        //不用做缓存击穿
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            if(jedis!=null){
                /**
                 * 先通过用户名找密码，然后通过用户名密码找用户信息
                 * */
                //User:password:info    username
                String userinfo = jedis.get("user:" + umsMember.getPassword() + ":info");
                if(StringUtils.isNotBlank(userinfo)){
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(userinfo, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //redis中没有找到缓存或者连接redis失败
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if(umsMemberFromDb!=null){
                jedis.setex("user:" + umsMember.getPassword() + ":info",60*60*24,JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;

        }finally {
            jedis.close();
        }

    }

    @Override
    public void addUserToken(String token, String id) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + id + ":token",60*60*24,token);
        jedis.close();
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> select = userMapper.select(umsMember);
        if(select!=null && select.size()>=0){
            return select.get(0);
        }
        return null;
    }
}
