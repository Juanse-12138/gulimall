package com.hyl.gulimall.gulimallauthserver.feign;

import com.hyl.common.utils.R;
import com.hyl.gulimall.gulimallauthserver.vo.UserLoginVo;
import com.hyl.gulimall.gulimallauthserver.vo.UserRegistVo;
import org.apache.catalina.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);
}
