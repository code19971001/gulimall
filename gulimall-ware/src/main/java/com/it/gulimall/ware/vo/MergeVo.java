package com.it.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/16 9:53
 */
@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;

}
