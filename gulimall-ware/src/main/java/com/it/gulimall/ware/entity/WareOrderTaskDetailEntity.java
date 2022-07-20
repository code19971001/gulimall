package com.it.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
@Data
@TableName("wms_ware_order_task_detail")
@AllArgsConstructor
@NoArgsConstructor
public class WareOrderTaskDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     *
     */
    private Integer skuNum;
    /**
     *
     */
    private Long taskId;

    private Long wareId;

	/**
	 * 1-锁定，2-解锁，3-扣减
	 */
	private Integer lockStatus;

}
