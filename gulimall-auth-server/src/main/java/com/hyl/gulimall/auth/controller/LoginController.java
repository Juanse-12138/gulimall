package com.hyl.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.constant.AuthServerConstant;
import com.hyl.common.exception.BizCodeEnume;
import com.hyl.common.utils.R;
import com.hyl.common.vo.MemberRespVo;
import com.hyl.gulimall.auth.feign.MemberFeignService;
import com.hyl.gulimall.auth.feign.ThirdPartyFeignService;
import com.hyl.gulimall.auth.vo.UserLoginVo;
import com.hyl.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hyl_marco
 * @data 2022/5/9 - 17:50
 *
 * TODO：商城其他页面的登录状态，目前只做了主页、商品详情页，其他也是相同的从Redis的Session中读取
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        /**
         * TODO:1.接口防刷
         *
         */

        /**
         * 2.验证码的再次校验 redis
         *
         */
        //先查当前是否已经有相应用户的验证码在缓存里
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long redisCodeTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - redisCodeTime < 60000) {
                //60s内不能重发
                return R.error(BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 6);
        String codeWithTime = code + "_" + System.currentTimeMillis();
        //redis缓存验证码，防止同一个用户在60s内再次发送验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeWithTime, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code);

        return R.ok();
    }


    //模拟重定向携带数据RedirectAttributes
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        //1.校验数据结果
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));

            redirectAttributes.addFlashAttribute("errors", errors);
            /**
             * i.这个方法是一个POST请求，转发是原请求原封不动转发给下一个，但路径映射默认是GET方式访问的，因此不能用/reg.html,要用reg
             * ii.但是转发依旧存在问题，会重复提交表单
             * iii.因此如果校验出错,重定向到注册页，
             * iv.这里利用Session原理，将数据放在session中，只要跳到下一个取出这个数据后，session里面的数据就会删掉.todo:分布式下的session问题
             * v.重定向默认按当前端口，应加上完整域名
             */
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //2.校验验证码
        String code = vo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                //验证码通过
                //删除验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //调用远程服务,注册
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    //注册成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    //前端传来k,v参数不需要加@RequestBody
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        //远程登录
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    /**
     * 重新处理登录页逻辑，如果已经登录了直接重定向到商城首页
     * @return
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession httpSession){
        Object user = httpSession.getAttribute(AuthServerConstant.LOGIN_USER);
        if(user != null){
            return "redirect:http://gulimall.com";
        }
        return "login";
    }
}
