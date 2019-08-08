package com.hangzhang.gmall.gmalluser.service.impl;

import com.hangzhang.gmall.gmalluser.bean.UmsMember;
import com.hangzhang.gmall.gmalluser.mapper.UserMapper;
import com.hangzhang.gmall.gmalluser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public boolean selectUser() {
        return false;
    }

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList= userMapper.selectAllUser();
        return umsMemberList;
    }
}
