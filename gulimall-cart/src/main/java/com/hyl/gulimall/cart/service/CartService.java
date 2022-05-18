package com.hyl.gulimall.cart.service;

import com.hyl.gulimall.cart.vo.Cart;
import com.hyl.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @author hyl_marco
 * @data 2022/5/15 - 17:08
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;


    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

}
