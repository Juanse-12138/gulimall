package com.hyl.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.common.utils.HttpUtils;
import com.hyl.common.utils.R;
import com.hyl.gulimall.auth.feign.MemberFeignService;
import com.hyl.common.vo.MemberRespVo;
import com.hyl.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hyl_marco
 * @data 2022/5/13 - 21:47
 */
//社交登录
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "4167729449");
        map.put("client_secret", "dcb360b4fd059f46ddaea6b429b818f7");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //根据code换取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header,query,map);
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //知道当前是哪个社交用户
            //1.如果是第一次登录,自动注册(为当前社交用户生成一个当前会员信息账号）
            //登录或者注册这个社交用户
            R oauthlogin = memberFeignService.socialLogin(socialUser);
            if (oauthlogin.getCode() == 0) {
                MemberRespVo data = oauthlogin.getData(new TypeReference<MemberRespVo>() {
                });
                System.out.println("登录成功"+data);
                log.info("登录成功:用户:{}",data.toString());

                //登录成功调回首页
                //第一次使用session,命令浏览器保存卡号,JsessionId的cookie
                //子域之间，发卡的时候(指定域名为父域名,看spring文档写配置类),即使是子域发卡,父域也可使用
                //注意序列化机制的修改，将Session中对象的序列化机制改成Json
                session.setAttribute("loginUser",data);
                servletResponse.addCookie(new Cookie("JSESSIONID","data"));
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
