package com.it.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/27 23:30
 */
@Data
public class SeckillSessionsWithSkus {

    private Long id;

    private String name;

    private Date startTime;

    private Date endTime;

    private Integer status;

    private Date createTime;

    private List<SeckillSkuRelationVo> relationSkus;

}
