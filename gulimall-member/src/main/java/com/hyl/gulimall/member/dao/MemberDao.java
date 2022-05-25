package com.hyl.gulimall.member.dao;

import com.hyl.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:53:25
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
