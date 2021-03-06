package com.hyl.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.hyl.common.exception.BizCodeEnume;
import com.hyl.gulimall.member.exception.PhoneExistException;
import com.hyl.gulimall.member.exception.UserExistException;
import com.hyl.gulimall.member.feign.CouponFeignService;
import com.hyl.gulimall.member.vo.MemberLoginVo;
import com.hyl.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.member.entity.MemberEntity;
import com.hyl.gulimall.member.service.MemberService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;



/**
 * 会员
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:53:25
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;


    //注册
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        } catch (UserExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    //登录
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity=memberService.login(vo);
        if(memberEntity!=null) {
            R r=new R();
            r.setData(memberEntity);
            return r;
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("member",memberEntity).put("coupons",memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
