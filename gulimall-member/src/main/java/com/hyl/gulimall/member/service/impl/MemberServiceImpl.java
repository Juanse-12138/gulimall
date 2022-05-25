package com.hyl.gulimall.member.service.impl;

import com.hyl.gulimall.member.dao.MemberLevelDao;
import com.hyl.gulimall.member.entity.MemberLevelEntity;
import com.hyl.gulimall.member.exception.PhoneExistException;
import com.hyl.gulimall.member.exception.UserExistException;
import com.hyl.gulimall.member.service.MemberLevelService;
import com.hyl.gulimall.member.vo.MemberLoginVo;
import com.hyl.gulimall.member.vo.MemberRegistVo;
import org.apache.catalina.User;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.member.dao.MemberDao;
import com.hyl.gulimall.member.entity.MemberEntity;
import com.hyl.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Autowired
    MemberDao memberDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) throws PhoneExistException, UserExistException {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity level=memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(level.getId());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setPassword(vo.getPassword());
        memberEntity.setMobile(vo.getPhone());
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        //密码加密保存
        String p= new BCryptPasswordEncoder().encode(vo.getPassword());
        memberEntity.setPassword(p);
        //其他默认信息设置 略
        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer mobile=baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile",phone));
        if(mobile>0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String username) throws UserExistException {
        Integer user=baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username",username));
        if(user>0) {
            throw new UserExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        //去数据库按照账号密码进行查询
        String loginacct=vo.getLoginacct();
        String password=vo.getPassword();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if(memberEntity==null) {
            return null;
        }else {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean match = bCryptPasswordEncoder.matches(password, memberEntity.getPassword());
            if(match) {
                return memberEntity;
            }else {
                return null;
            }
        }
    }

}