package com.it.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.common.utils.PageUtils;
import com.it.gulimall.ware.entity.PurchaseEntity;
import com.it.gulimall.ware.vo.MergeVo;
import com.it.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchases(Map<String, Object> params);

    void mergePurchases(MergeVo mergeVo);

    void receivePurchases(List<Long> ids);

    void donePurchases(PurchaseDoneVo doneVo);
}

