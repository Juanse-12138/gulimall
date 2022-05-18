package com.hyl.gulimall.thirdparty.controller;

import com.hyl.common.utils.R;
import com.hyl.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hyl_marco
 * @data 2022/5/9 - 17:33
 *
 * 这个controller是供其他服务调用，而不是直接由前端调用；
 */
@RequestMapping("/sms")
@RestController
public class SmsSendController {
    @Autowired
    private SmsComponent smsComponent;

//    @Autowired
//    private MyAccess access;

    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code){
        smsComponent.sendSms(phone,code);
        return R.ok();
    }

}
