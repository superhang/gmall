package com.service;


import com.beans.UmsMember;
import com.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    public boolean selectUser();

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String menberId);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String id);
}
