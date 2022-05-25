package com.hyl.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.member.entity.MemberEntity;
import com.hyl.gulimall.member.exception.PhoneExistException;
import com.hyl.gulimall.member.exception.UserExistException;
import com.hyl.gulimall.member.vo.MemberLoginVo;
import com.hyl.gulimall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:53:25
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo) throws PhoneExistException,UserExistException;
    void checkPhoneUnique(String phone) throws PhoneExistException;
    void checkUserNameUnique(String username) throws UserExistException;

    MemberEntity login(MemberLoginVo vo);
}

