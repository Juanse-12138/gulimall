package com.hyl.gulimall.ware.service.impl;

import com.hyl.common.constant.WareConstant;
import com.hyl.gulimall.ware.entity.PurchaseDetailEntity;
import com.hyl.gulimall.ware.service.PurchaseDetailService;
import com.hyl.gulimall.ware.service.WareSkuService;
import com.hyl.gulimall.ware.vo.MergeVo;
import com.hyl.gulimall.ware.vo.PurchaseDoneVo;
import com.hyl.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.ware.dao.PurchaseDao;
import com.hyl.gulimall.ware.entity.PurchaseEntity;
import com.hyl.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.constraints.NotNull;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        /*TODO:这里要确认一下选择的采购单状态是正确的*/
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(finalPurchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void receive(List<Long> ids) {
        /*确认当前采购单是新建或已分配状态*/
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.BUYING.getCode());
            item.setUpdateTime(new Date());
            return item;
            }
        ).collect(Collectors.toList());

        if(!collect.isEmpty()){
            /*改变采购单的状态*/
            this.updateBatchById(collect);

            /*改变采购需求的状态*/
            collect.forEach(item ->{
                List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
                List<PurchaseDetailEntity> detailEntitiesWithStatus = detailEntities.stream().map(entity -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                    purchaseDetailEntity.setId(entity.getId());
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.BUYING.getCode());
                    return purchaseDetailEntity;
                }).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(detailEntitiesWithStatus);
            });


        }


    }

    @Transactional
    @Override
    public void purchaseDone(PurchaseDoneVo purchaseDoneVo) {
        /*改变采购单状态*/
        Long id = purchaseDoneVo.getId();

        /*TODO:数据库缺少失败原因字段，以及细化 应采购 实采购部分*/
        Boolean flag = true;
        ArrayList<PurchaseDetailEntity> detailEntities = new ArrayList<>();
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchaseStatusEnum.ERRORED.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.ERRORED.getCode());
            }else{
                purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISHED.getCode());
                /*将成功的采购需求进行入库*/
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }

            purchaseDetailEntity.setId(item.getItemId());
            detailEntities.add(purchaseDetailEntity);

        }
        purchaseDetailService.updateBatchById(detailEntities);

        /*改变采购单状态*/
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.ERRORED.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}