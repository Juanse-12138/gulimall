package com.hyl.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hyl_marco
 * @data 2022/3/24 - 15:12
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
