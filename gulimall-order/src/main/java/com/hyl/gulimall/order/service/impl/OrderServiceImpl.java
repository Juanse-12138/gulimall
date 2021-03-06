package com.hyl.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hyl.common.exception.NoStockException;
import com.hyl.common.to.mq.OrderTo;
import com.hyl.common.to.mq.SeckillOrderTo;
import com.hyl.common.utils.R;
import com.hyl.common.vo.MemberRespVo;
import com.hyl.gulimall.order.constant.OrderConstant;
import com.hyl.gulimall.order.entity.OrderItemEntity;
import com.hyl.gulimall.order.enume.OrderStatusEnum;
import com.hyl.gulimall.order.feign.CartFeignService;
import com.hyl.gulimall.order.feign.MemberFeignService;
import com.hyl.gulimall.order.feign.ProductFeignService;
import com.hyl.gulimall.order.feign.WareFeignService;
import com.hyl.gulimall.order.interceptor.LoginUserInterceptor;
import com.hyl.gulimall.order.service.OrderItemService;
import com.hyl.gulimall.order.to.OrderCreateTo;
import com.hyl.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.order.dao.OrderDao;
import com.hyl.gulimall.order.entity.OrderEntity;
import com.hyl.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal=new ThreadLocal<>();

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo member = LoginUserInterceptor.loginUser.get();
        //1?????????????????????????????????
        List<MemberAddressVo> address=memberFeignService.getAddress(member.getId());
        orderConfirmVo.setAddress(address);
        for(MemberAddressVo a:address) {
            System.out.println(a);
        }
        //2???????????????????????????????????????
        List<OrderItemVo> items=cartFeignService.getCurrentUserCartItems();
        orderConfirmVo.setItems(items);
        //3???????????????????????????
        Integer integration=member.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //4?????????????????????

        //5?????????????????????
        String token= UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+member.getId(),token,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        return  orderConfirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        confirmVoThreadLocal.set(submitVo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        response.setCode(0);
        // 1??????????????????????????????????????????????????????????????????
        // 0???????????? -1????????????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        // ?????????????????????????????????
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L) {
            // ??????????????????
            response.setCode(1);
            return response;
        } else {
            // ?????????????????? ?????? ??????????????? ???????????? ???????????? ????????????
            // 1????????????????????????????????????
            OrderCreateTo order = createOrder();
            // 2?????????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) <0.01){
                // ??????????????????
                // 3???????????????
                saveOrder(order);
                // 4???????????????,??????????????????????????????????????????????????????????????????skuId,skuName,num???
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    orderItemVo.setPrice(item.getSkuPrice());
                    orderItemVo.setImage(item.getSkuPic());
//                    orderItemVo.setSkuAttr(item.getSkuAttrsVals());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVos);
                // TODO ???????????????
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0){
                    //????????????
                    response.setOrder(order.getOrder());
                    return response;
                }else {
                    //????????????
                    throw new NoStockException((String) r.get("msg"));
                }
            }else {
                response.setCode(2);
                return response;
            }
        }
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId());
//        if (orderToken != null && orderToken.equals(redisToken)){
//            //??????????????????
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId());
//        }else {
//            //?????????
//        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override
    public void closeOrder(OrderEntity order) {
        OrderEntity orderEntity=this.getById(order.getId());
        if(orderEntity.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity update=new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo=new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
        }
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());
        orderItemService.save(orderItemEntity);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //?????????????????????
        String timeId = IdWorker.getTimeId().substring(0,32);
        OrderEntity orderEntity = buildOrder(timeId);
        createTo.setOrder(orderEntity);
        // 2???????????????????????????
        List<OrderItemEntity> itemEntities = buildOrderItems(timeId);
        createTo.setOrderItems(itemEntities);
        // 3?????????????????????????????????
        computePrice(orderEntity,itemEntities);
        return createTo;

    }

    /**
     * ???????????????????????????
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // ????????????????????????????????????
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size()>0){
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * ????????????????????????
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1 ???????????? ?????????
        // 2 SPU??????
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>(){});
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());
        // 3 SKU??????
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";"); //???????????????????????????
        orderItemEntity.setSkuAttrsVals(skuAttrs);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        // 4 ???????????? [??????]

        // 5 ????????????
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        // 6 ????????????????????????
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        // ??????????????????????????????
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        // ????????????????????????????????????
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

    /**
     * ??????????????????
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }


    /**
     * ????????????
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberResponseVo.getId());
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // ????????????????????????
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = r.getData(new TypeReference<FareVo>() {
        });
        // ??????????????????
        orderEntity.setFreightAmount(fareResp.getFare());
        // ?????????????????????
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());
        // ?????????????????????????????????
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        return orderEntity;
    }

    /**
     * ??????????????????
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        // ????????????????????????????????????????????????????????????
        for (OrderItemEntity entity : itemEntities) {
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));
        }
        // ??????????????????
        orderEntity.setTotalAmount(total);
        // ????????????
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        // ??????????????????
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        // ?????????????????? 0?????????
        orderEntity.setDeleteStatus(0);
    }

}