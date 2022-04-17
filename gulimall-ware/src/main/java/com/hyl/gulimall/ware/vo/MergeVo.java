package com.hyl.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 14:09
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
