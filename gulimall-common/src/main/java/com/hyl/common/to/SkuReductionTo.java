package com.hyl.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/3/24 - 15:31
 */
@Data
public class SkuReductionTo {
    private Long skuId;

    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private BigDecimal priceStatus;
    private List<MemberPrice> memberPrice;
}
