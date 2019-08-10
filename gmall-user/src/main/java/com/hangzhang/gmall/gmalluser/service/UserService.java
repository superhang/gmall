package com.hangzhang.gmall.gmalluser.service;

import com.hangzhang.gmall.gmalluser.bean.UmsMember;
import com.hangzhang.gmall.gmalluser.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    public boolean selectUser();

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String menberId);
}
