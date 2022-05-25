package com.hyl.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {

    private Long id;//库存工作单Id
//    private List<Long> detailId;

    private StockDetailTo detail;//工作单详情的所有Id
}

