package com.hangzhang.gmall.gmalluserservice.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.beans.UmsMember;
import com.beans.UmsMemberReceiveAddress;
import com.hangzhang.gmall.gmalluserservice.mapper.UmsMemberReceiveAddressMapper;
import com.hangzhang.gmall.gmalluserservice.mapper.UserMapper;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
//使用rpc，dubbo扫描
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
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
}
