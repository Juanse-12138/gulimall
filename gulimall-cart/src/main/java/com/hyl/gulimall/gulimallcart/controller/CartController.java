package com.hyl.gulimall.gulimallcart.controller;

import com.hyl.common.constant.AuthServerConstant;
import com.hyl.gulimall.gulimallcart.interceptor.CartInterceptor;
import com.hyl.gulimall.gulimallcart.service.CartService;
import com.hyl.gulimall.gulimallcart.vo.Cart;
import com.hyl.gulimall.gulimallcart.vo.CartItem;
import com.hyl.gulimall.gulimallcart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    /**
     * 浏览器本地保存一个cookie，里面保存一个user-key，设置一个月过期
     * @param session
     * @return
     */
    @Autowired
    CartService cartService;

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check) {
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    //修改购物车项数量http://cart.gulimall.com/countItem?skuId=1&num=2
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num) {
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    //删除购物车项http://cart.gulimall.com/deleteItem?skuId=2
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num, Model model) throws ExecutionException, InterruptedException {
        CartItem cartItem=cartService.addToCart(skuId,num);
        model.addAttribute("items",cartItem);
        return "success";
    }

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }
}
