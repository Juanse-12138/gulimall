package com.hyl.gulimall.gulimallcart.service;

import com.hyl.gulimall.gulimallcart.vo.Cart;
import com.hyl.gulimall.gulimallcart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    Cart getCart() throws ExecutionException, InterruptedException;

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
