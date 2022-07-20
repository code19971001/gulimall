package com.it.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.it.common.valid.AddGroup;
import com.it.common.valid.ListValue;
import com.it.common.valid.UpdateGroup;
import com.it.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 01:36:30
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
    @Null(message = "新增不能指定id", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名字必须提交", groups = {UpdateGroup.class, AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {UpdateGroup.class, AddGroup.class})
    private String logo;
    /**
     * 介绍
     */
    @NotBlank(message = "必须填写descript信息", groups = {AddGroup.class})
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "首字母不能为空", groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序字段不能为空", groups = {AddGroup.class})
    @Min(value = 0L, message = "排序必须大于等于0L", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
