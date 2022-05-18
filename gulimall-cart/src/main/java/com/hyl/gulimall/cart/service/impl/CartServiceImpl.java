package com.hyl.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.common.utils.R;
import com.hyl.gulimall.cart.feign.ProductFeignService;
import com.hyl.gulimall.cart.interceptor.CartInterceptor;
import com.hyl.gulimall.cart.service.CartService;
import com.hyl.gulimall.cart.vo.Cart;
import com.hyl.gulimall.cart.vo.CartItem;
import com.hyl.gulimall.cart.vo.SkuInfoVo;
import com.hyl.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author hyl_marco
 * @data 2022/5/15 - 17:09
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "gulimall:cart:";

    /**
     * 1. redis中缓存购物车信息的前缀写成final string
     * 2. 配置线程池实现多线程查询
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //1.抽取成一个方法
        //获得当前的用户信息
        //cartKey的值，登录了UserId，没登录临时的UserKey
        //当前购物车是否有当前sku
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());

        if(StringUtils.isEmpty(res)){
            //2.如果没有当前商品，远程查询当前要查询商品的信息,得到skuInfoVo,封装为异步任务
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                //3.封装cartItem
                //3.1 普通信息
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, threadPoolExecutor);

            //3.2 远程查询sku的组合属性信息，封装为异步任务
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, threadPoolExecutor);

            //4.将查询到的转成Json数据放入redis(这里要等异步任务结束)
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        }else{
            //5.如果购物车里有此商品，修改数量即可
            //将json逆转为CartItem
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            //再将CartItem换回json存入redis
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String itemStr = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(itemStr, CartItem.class);
        return cartItem;
     }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        //1.登录状态，是登录用户还是临时用户
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = new Cart();
        //这里将获取购物车的代码抽取为单独的方法
        //2.临时用户购物车
        String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
        List<CartItem> tempCartItems = getCartItems(tempCartKey);
        if(userInfoTo.getUserId() != null){
            //3.登录状态
            String userCartKey = CART_PREFIX + userInfoTo.getUserId();
            //3.1如果有临时购物车,需要合并用户购物车和临时购物车
            if(tempCartItems != null){
                for(CartItem cartItem : tempCartItems){
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //合并之后清除临时购物车
                clearCart(tempCartKey);
            }
            //3.2再查合并后的用户购物车
            List<CartItem> userCartItems = getCartItems(userCartKey);
            cart.setItems(userCartItems);
        }else {
            //4.没有登录直接返回临时购物车里的内容
            cart.setItems(tempCartItems);
        }
        return cart;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 改变sku是否选中信息
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String cartItemStr = JSON.toJSONString(cartItem);
        cartOps.put(skuId,cartItem);

    }

    /**
     * 改变sku数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    /**
     * 删除商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获得当前购物车的key
     * 1.获得当前的用户信息
     * 2.cartKey的值，登录了UserId，没登录临时的UserKey
     * 3.当前购物车是否有当前sku
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //判断真实用户还是临时用户
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //绑定key,以后所有redis操作都针对此key
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(cartKey);
        return operation;
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if(values != null && values.size() > 0){
            List<CartItem> cartItems = values.stream().map((obj) -> {
                String item = (String) obj;
                CartItem cartItem = JSON.parseObject(item, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }
}
