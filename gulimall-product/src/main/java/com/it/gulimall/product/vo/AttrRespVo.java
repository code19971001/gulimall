package com.it.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : code1997
 * @date : 2021/5/6 23:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AttrRespVo extends AttrVo {

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

}
