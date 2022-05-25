package com.hyl.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.utils.R;
import com.hyl.gulimall.ware.feign.MemberFeignService;
import com.hyl.gulimall.ware.vo.FareVo;
import com.hyl.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.ware.dao.WareInfoDao;
import com.hyl.gulimall.ware.entity.WareInfoEntity;
import com.hyl.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
//        IPage<WareInfoEntity> page = this.page(
//                new Query<WareInfoEntity>().getPage(params),
//                new QueryWrapper<WareInfoEntity>()
//        );

        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long attrId) {

        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(attrId);
        MemberAddressVo data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if (data != null){
            //模拟计算运费
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);

            return fareVo;
        }
        return null;
    }
}