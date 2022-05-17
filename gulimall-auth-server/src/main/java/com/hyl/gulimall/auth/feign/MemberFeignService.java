package com.hyl.gulimall.auth.feign;

import com.hyl.common.utils.R;
import com.hyl.gulimall.auth.vo.SocialUser;
import com.hyl.gulimall.auth.vo.UserLoginVo;
import com.hyl.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hyl_marco
 * @data 2022/5/9 - 21:57
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("member/member/oauth2/login")
    public R socialLogin(@RequestBody SocialUser vo);
}
