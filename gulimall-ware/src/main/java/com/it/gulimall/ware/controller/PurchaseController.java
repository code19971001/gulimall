package com.it.gulimall.ware.controller;

import com.it.common.utils.PageUtils;
import com.it.common.utils.R;
import com.it.gulimall.ware.entity.PurchaseEntity;
import com.it.gulimall.ware.service.PurchaseService;
import com.it.gulimall.ware.vo.MergeVo;
import com.it.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/done")
    public R donePurchases(@RequestBody PurchaseDoneVo doneVo) {

        purchaseService.donePurchases(doneVo);
        return R.ok();
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {

        purchaseService.mergePurchases(mergeVo);
        return R.ok();
    }

    @PostMapping("/received")
    public R receivePurchases(@RequestBody List<Long> ids) {
        purchaseService.receivePurchases(ids);
        return R.ok();
    }

    @GetMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params) {

        PageUtils page = purchaseService.queryPageUnreceivePurchases(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
