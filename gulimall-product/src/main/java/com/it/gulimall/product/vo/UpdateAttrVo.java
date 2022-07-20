package com.it.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : code1997
 * @date : 2021/5/16 23:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateAttrVo extends Attr {
    private Integer quickShow;
}
