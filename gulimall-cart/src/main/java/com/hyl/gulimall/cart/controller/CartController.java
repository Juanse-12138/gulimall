package com.hyl.gulimall.cart.controller;

import com.hyl.gulimall.cart.interceptor.CartInterceptor;
import com.hyl.gulimall.cart.service.CartService;
import com.hyl.gulimall.cart.vo.Cart;
import com.hyl.gulimall.cart.vo.CartItem;
import com.hyl.gulimall.cart.vo.UserInfoTo;
import jdk.nashorn.internal.runtime.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author hyl_marco
 * @data 2022/5/15 - 17:11
 */

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 1. 浏览器有一个cookie→user-key，用以标识用户身份，过期时间一个月
     * 2. 第一次使用购物车需要获得一个user-key，存放在浏览器
     *
     * 登录情况：session里有
     * 没登陆情况：用cookie里的
     * 第一次：没有临时用户，创建一个
     * 3. 封装拦截器实现该功能
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        //1.判断当前是否登录（这一块放到拦截器做了）

        //2.获取当前的用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //3.查询当前用户的购物车
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }


    /**
     * 1. 请求必须带的参数skuId，添加到购物车的个数num
     * 2. 在业务层编写将当前sku加入到购物车的方法
     * 3. 需要返回的是当前购物项的详细信息CartItem,并将其放在请求域中
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
        ra.addAttribute("skuId",skuId);

        //这里如果直接返回成功页面会存在问题，重复刷新时反复提交该请求
        // 这里应该重定向到一个幂等的请求中，再返回success
//        return "success";
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 1. 重定向不能用Model，转发才可以
     * 2. 前一个方法用RedirectAttributes,
     *     ra.addFlashAttribute 原理时将数据放在session中，可以从页面取出，但只能取一次
     *      ra.addAttribute 将数据拼接在url后面
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("items", item);
        return "success";
    }

    /**
     * TODO：这里的购物车商品局部修改可以优化
     * 改变sku的是否勾选信息
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变购物车中商品的数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num")Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除购物车中的商品
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
