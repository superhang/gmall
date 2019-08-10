package com.hangzhang.gmall.gmalluser.controller;


import com.beans.UmsMember;
import com.beans.UmsMemberReceiveAddress;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;


    @RequestMapping("/selectuser")
    @ResponseBody
    public boolean selectUserById(){
        return false;
    }
    @RequestMapping("/selectAllUser")
    @ResponseBody
    public List<UmsMember> selectAllUser(){
        List<UmsMember> userList = userService.getAllUser();
        return userList;
    }
    @RequestMapping("/getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String MenberId){
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress = userService.getReceiveAddressByMemberId(MenberId);
        return umsMemberReceiveAddress;
    }
}
