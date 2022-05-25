package com.hyl.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.hyl.common.utils.PageUtils;
import lombok.Data;

import java.security.PrivateKey;

/**
 * @author hyl_marco
 * @data 2022/3/19 - 15:02
 */
/*TODO：请求体和响应体建议单独加包*/

@Data
public class AttrRespVo extends AttrVo{
    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
