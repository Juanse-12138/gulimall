package com.hyl.gulimall.ssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

@Controller
public class HelloController {
    /**
     * 无需登录即可访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    public String hi(HttpSession session) {
        Object user=session.getAttribute("loginUser");
        if(user==null) {
            //没有登录，跳转到登陆页面
        }
        return "Hi";
    }
}
