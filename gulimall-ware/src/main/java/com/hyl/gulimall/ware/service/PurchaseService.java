package com.hyl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.ware.entity.PurchaseEntity;
import com.hyl.gulimall.ware.vo.MergeVo;
import com.hyl.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:17:39
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceived(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void receive(List<Long> ids);

    void purchaseDone(PurchaseDoneVo purchaseDoneVo);
}

