package com.hyl.gulimall.gulimallcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.common.utils.R;
import com.hyl.gulimall.gulimallcart.feign.ProductFeignService;
import com.hyl.gulimall.gulimallcart.interceptor.CartInterceptor;
import com.hyl.gulimall.gulimallcart.service.CartService;
import com.hyl.gulimall.gulimallcart.vo.Cart;
import com.hyl.gulimall.gulimallcart.vo.CartItem;
import com.hyl.gulimall.gulimallcart.vo.SkuInfoVo;
import com.hyl.gulimall.gulimallcart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX="gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //判断购物车中是否有该商品，如果没有，查询添加
        String o =(String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(o)) {
            CartItem cartItem=new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R info = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                //将商品添加到购物车
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setSkuId(skuInfo.getSkuId());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
            },executor);
            CompletableFuture<Void> getAttrList = CompletableFuture.runAsync(() -> {
                //远程查询sku的属性信息
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);
            CompletableFuture.allOf(getSkuInfo,getAttrList).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
            return cartItem;
        } else {
            //购物车中有该商品，只改变数量选择
            CartItem cartItem = JSON.parseObject(o, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
        //远程查询商品信息


    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        //得到用户信息 账号用户 、临时用户
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1、userInfoTo.getUserId()不为空表示账号用户，反之临时用户  然后决定用临时购物车还是用户购物车
        //放入缓存的key
        String cartKey = "";
        if (userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    /**
     * 获取要操作的购物车
     * @return
     */
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null) {
            //已登录，将游客购物车进行合并
            String cartkey=CART_PREFIX+userInfoTo.getUserId();
            String tempCartKey=CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> tempCartItems=getCartItems(tempCartKey);
            if(tempCartItems!=null) {
                for(CartItem item:tempCartItems){
                    addToCart(item.getSkuId(),item.getCount());
                }
                clearCart(tempCartKey);
            }
            List<CartItem> cartItems=getCartItems(cartkey);
            cart.setItems(cartItems);

        }else {
            //未登录，获取临时购物车
            String cartkey=CART_PREFIX+userInfoTo.getUserKey();
            cart.setItems(getCartItems(cartkey));
        }
        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem= JSON.parseObject((String)cartOps.get(skuId.toString()),CartItem.class);
        if(check.equals(1)) {
            cartItem.setCheck(true);
        }
        else {
            cartItem.setCheck(false);
        }
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem= JSON.parseObject((String)cartOps.get(skuId.toString()),CartItem.class);
        cartItem.setCount(num);
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        String cartkey=CART_PREFIX+CartInterceptor.threadLocal.get().getUserId();
        List<CartItem> cartItems = getCartItems(cartkey);
        List<CartItem> cartItemList=new ArrayList<>();
        if(cartItems!=null&&cartItems.size()>0) {
            for(CartItem item:cartItems) {
                if(item.getCheck()) {
                    cartItemList.add(item);
                }
            }
        }
        return cartItemList;
    }

    private void clearCart(String tempCartKey) {
        redisTemplate.delete(tempCartKey);
    }

    private List<CartItem> getCartItems(String cartkey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartkey);
        List<Object> values = operations.values();
        List<CartItem> cartItems=new ArrayList<>();
        if(values!=null&&values.size()>0) {
            for(Object val:values){
                CartItem cartItem = JSON.parseObject((String) val, CartItem.class);
                cartItems.add(cartItem);
            }
        }
        return cartItems;
    }
}
