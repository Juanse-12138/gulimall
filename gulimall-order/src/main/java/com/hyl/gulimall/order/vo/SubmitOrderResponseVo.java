package com.hyl.gulimall.order.vo;

import com.hyl.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;
}
