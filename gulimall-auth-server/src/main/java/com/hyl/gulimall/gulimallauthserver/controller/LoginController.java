package com.hyl.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.constant.AuthServerConstant;
import com.hyl.common.utils.R;
import com.hyl.common.vo.MemberRespVo;
import com.hyl.gulimall.gulimallauthserver.feign.MemberFeignService;
import com.hyl.gulimall.gulimallauthserver.vo.UserLoginVo;
import com.hyl.gulimall.gulimallauthserver.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if(result.hasErrors()){
            Map<String,String> errors=result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }else{
            //真正的注册调用远程服务进行注册
            R r = memberFeignService.regist(vo);
            if(r.getCode()==0) {
                //注册成功
                System.out.println("注册成功");
                return "redirect:http://auth.gulimall.com//login.html";
            } else {
                Map<String,String> errors=new HashMap<>();
                errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        //发送给远程服务进行登录
        R r = memberFeignService.login(vo);
        if(r.getCode()==0) {
            //登录成功
            log.info("登录成功：用户{}"+vo.getLoginacct());
            MemberRespVo data=r.getData("data",new TypeReference<MemberRespVo>(){});
            session.setAttribute("loginUser",data);
            return "redirect:http://gulimall.com";
        }else {
            Map<String,String> errors=new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com//login.html";
        }
    }
}
