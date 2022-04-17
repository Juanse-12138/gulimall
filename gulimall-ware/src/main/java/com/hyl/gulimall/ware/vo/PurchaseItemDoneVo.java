package com.hyl.gulimall.ware.vo;

import lombok.Data;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 16:04
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
