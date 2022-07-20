package com.it.gulimall.ware.controller;

import com.it.common.exception.BizCodeEnume;
import com.it.common.exception.NoStockException;
import com.it.common.to.SkuHasStockVo;
import com.it.common.utils.PageUtils;
import com.it.common.utils.R;
import com.it.gulimall.ware.entity.WareSkuEntity;
import com.it.gulimall.ware.service.WareSkuService;
import com.it.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 为某一个订单锁定库存
     */
    @PostMapping("lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo lockVo) {
        try {
            boolean result = wareSkuService.lockStock(lockVo);
            return R.ok().setData(result);
        } catch (NoStockException e) {
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 查询指定的sku是否存在库存
     */
    @PostMapping("/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVo> hasStock = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(hasStock);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
