package com.hangzhang.gmall.gmallpassportweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PassportController {
    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
