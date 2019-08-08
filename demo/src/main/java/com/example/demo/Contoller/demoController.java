package com.example.demo.Contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class demoController {
    @ResponseBody
    @RequestMapping("/hello")
    public String gethelloword(){
        return "hello world";
    }

}
