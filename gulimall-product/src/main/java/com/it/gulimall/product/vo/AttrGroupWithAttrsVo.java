package com.it.gulimall.product.vo;

import com.it.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/11 23:12
 */
@Data
public class AttrGroupWithAttrsVo {

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
