package com.hangzhang.gmall.gmalluser.service;

import com.hangzhang.gmall.gmalluser.bean.UmsMember;

import java.util.List;

public interface UserService {
    public boolean selectUser();

    List<UmsMember> getAllUser();
}
