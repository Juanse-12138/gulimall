package com.hyl.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 16:02
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;

    private List<PurchaseItemDoneVo> items;

}
