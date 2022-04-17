package com.hyl.gulimall.product.service.impl;

import com.hyl.gulimall.product.entity.AttrEntity;
import com.hyl.gulimall.product.service.AttrService;
import com.hyl.gulimall.product.vo.AttrGroupwithAttrsRespVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.AttrGroupDao;
import com.hyl.gulimall.product.entity.AttrGroupEntity;
import com.hyl.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)-> {
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if(catelogId != 0){
//            String key = (String) params.get("key");
            /*select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like %key%)*/
//            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
//            if(!StringUtils.isEmpty(key)){
//                wrapper.and((obj)-> {
//                    obj.eq("attr_group_id",key).or().like("attr_group_name",key);
//                });
//            }
            wrapper.eq("catelog_id", catelogId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupwithAttrsRespVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        /*需不需要加判空？*/
        List<AttrGroupwithAttrsRespVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupwithAttrsRespVo attrGroupwithAttrsRespVo = new AttrGroupwithAttrsRespVo();
            BeanUtils.copyProperties(group, attrGroupwithAttrsRespVo);
            List<AttrEntity> attrs = attrService.getAttrRelationAttr(attrGroupwithAttrsRespVo.getAttrGroupId());
            attrGroupwithAttrsRespVo.setAttrs(attrs);
            return attrGroupwithAttrsRespVo;
        }).collect(Collectors.toList());
        return collect;
    }

}