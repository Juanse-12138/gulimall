package com.hyl.gulimall.order.web;

import com.hyl.gulimall.order.service.OrderService;
import com.hyl.gulimall.order.vo.OrderConfirmVo;
import com.hyl.gulimall.order.vo.OrderSubmitVo;
import com.hyl.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) {
        OrderConfirmVo confirmOrder =orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmOrder);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo =orderService.submitOrder(submitVo);
        if(responseVo.getCode()==0) {
            //下单成功跳转支付页面
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }  else {
            String msg="下订单失败";
            switch (responseVo.getCode()) {
                case 1 : msg+="订单信息过期，请重新提交";break;
                case 2 : msg+="订单中商品价格发生变化，请刷新后重新提交";break;
                case 3 : msg+="库存锁定失败，商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
