package com.hyl.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.exception.NoStockException;
import com.hyl.common.to.mq.OrderTo;
import com.hyl.common.to.mq.StockDetailTo;
import com.hyl.common.to.mq.StockLockedTo;
import com.hyl.common.utils.R;
import com.hyl.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.hyl.gulimall.ware.entity.WareOrderTaskEntity;
import com.hyl.gulimall.ware.enume.OrderStatusEnum;
import com.hyl.gulimall.ware.enume.WareTaskStatusEnum;
import com.hyl.gulimall.ware.feign.OrderFeignService;
import com.hyl.gulimall.ware.feign.ProductFeignService;
import com.hyl.gulimall.ware.service.WareOrderTaskDetailService;
import com.hyl.gulimall.ware.service.WareOrderTaskService;
import com.hyl.gulimall.ware.vo.OrderItemVo;
import com.hyl.gulimall.ware.vo.SkuHasStockVo;
import com.hyl.gulimall.ware.vo.SkuWareHasStock;
import com.hyl.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.ware.dao.WareSkuDao;
import com.hyl.gulimall.ware.entity.WareSkuEntity;
import com.hyl.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }

        String wareId = (String)params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        /*1.判断是否有这个库存记录*/
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            /*远程获得sku的名字*/
            try {
                /*为了不让skuName这个冗余字段影响整个事物，用try,catch,如果失败不需要回滚*/
                /*TODO：其他方法？高级部分*/
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0){
                    Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(wareSkuEntity);
        }else{

            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查询当前sku总库存量
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count==null?false:count>0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * (rollbackFor = NoStockException.class)
     * 默认只要都是运行异常都会回滚
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1、按照下单的收货地址，找到一个就近仓库，锁定库存

        // 1、找到每个商品在哪个仓库都有库存
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪个仓库有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        // 2、锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0){
                //没有任何库存有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                //成功返回1；否则就是0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());
                if (count == 1){
                    skuStocked = true;
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null,skuId,"",hasStock.getNum(),taskEntity.getId(),wareId,1);
                    orderTaskDetailService.save(detailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity,stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked", stockLockedTo);
                    break;
                }
            }
            //当仓库锁失败，重试下一个仓库
            if (skuStocked == false){
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        // 3、肯定全部都是锁定成功的
        return true;
    }

    @Override
    public void unlock(StockLockedTo to) throws IOException {
        StockDetailTo detail = to.getDetail();
        WareOrderTaskDetailEntity detailEntity=orderTaskDetailService.getById(detail.getId());
        //如果工作单详情不为空，说明该库存锁定成功
        if(detailEntity!=null) {
            WareOrderTaskEntity taskEntity = orderTaskService.getById(to.getId());
            R r=orderFeignService.infoByOrderSn(taskEntity.getOrderSn());
            if(r.getCode()==0) {
                OrderTo order=r.getData(new TypeReference<OrderTo>(){});
                //没有该订单或者订单状态为 已取消 ==》解锁库存
                if(order==null||order.getStatus()== OrderStatusEnum.CREATE_NEW.getCode()) {
                    //保证幂等性，只有当工作单处于被锁定的情况下才进行解锁
                    if(detailEntity.getLockStatus()== WareTaskStatusEnum.Locked.getCode()) {
                        unlockStock(detail.getSkuId(),detail.getSkuNum(),detail.getWareId(),detailEntity.getId());
                    }
                }
            } else {
                throw new RuntimeException("远程调用订单服务失败");
            }
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态一直改变不了，库存消息优先到期，查订单状态新建状态，什么都不做就走了
     * 导致卡顿的订单，永远不能解锁库存
     * @param to
     */
    @Transactional
    @Override
    public void unLockStockForOrder(OrderTo to) {
        String orderSn = to.getOrderSn();
        //查一下最新的库存解锁状态，防止重复解锁库存
//        R r = orderFeignService.infoByOrderSn(orderSn);
//        if(r.getCode()==0) {
//            OrderTo order=r.getData(new TypeReference<OrderTo>(){});
//            if(order==null||order.getStatus()==OrderStatusEnum.CANCLED.getCode()) {
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作单找到所有 没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unlockStock(entity.getSkuId(),entity.getSkuNum(),entity.getWareId(),entity.getId());
        }
    }

    private void unlockStock(Long skuId, Integer skuNum, Long wareId, Long taskDetailId) {
        wareSkuDao.unlockStock(skuId,skuNum,wareId,taskDetailId);
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(taskDetailId);
        detailEntity.setLockStatus(WareTaskStatusEnum.UNLocked.getCode());
        orderTaskDetailService.updateById(detailEntity);
    }
}