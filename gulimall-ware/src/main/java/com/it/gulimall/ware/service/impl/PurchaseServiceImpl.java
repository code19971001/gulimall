package com.it.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.constant.WareConstant;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.gulimall.ware.dao.PurchaseDao;
import com.it.gulimall.ware.entity.PurchaseDetailEntity;
import com.it.gulimall.ware.entity.PurchaseEntity;
import com.it.gulimall.ware.service.PurchaseDetailService;
import com.it.gulimall.ware.service.PurchaseService;
import com.it.gulimall.ware.service.WareSkuService;
import com.it.gulimall.ware.vo.MergeVo;
import com.it.gulimall.ware.vo.PurchaseDoneVo;
import com.it.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchases(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    //todo:如何合并失败，给出前端页面提示：失败的原因
    @Transactional
    @Override
    public void mergePurchases(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            //默认是新建状态
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //确认采购单的状态是0，1才可以合并
        PurchaseEntity purchase = this.baseMapper.selectById(purchaseId);
        if (purchase.getStatus() != WareConstant.PurchaseStatusEnum.CREATED.getCode()
                && purchase.getStatus() != WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            log.error("采购单的为" + purchase.getId() + ",当前状态：" + purchase.getStatus() + "不能合并采购项到该单中");
            return;
        }

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        //合并的采购项只能是0，1的状态才可以进行合并
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream()
                .filter(id -> {
                            PurchaseDetailEntity byId = purchaseDetailService.getById(id);
                            return byId.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() ||
                                    byId.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
                        }
                )
                .map(item -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                    purchaseDetailEntity.setId(item);
                    purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                    return purchaseDetailEntity;
                }).collect(Collectors.toList());
        if (purchaseDetailEntities.isEmpty()) {
            log.error("采购项的状态不对，不存在可以合并整单的采购项,采购项id为：" + items);
            return;
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        //更新采购单的时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     * 条件：1.确定当前采购单是新建或者已分配状态。
     * 2。改变采购单的状态。
     * 过程：1.改变采购单的状态，
     * 2.改变采购项的状态。
     *
     * @param ids
     */
    @Override
    public void receivePurchases(List<Long> ids) {
        //1.修改采购单的状态
        List<PurchaseEntity> purchaseEntities = ids.stream()
                .map(this::getById)
                .filter(purchaseEntity -> purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(purchaseEntity -> {
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    purchaseEntity.setUpdateTime(new Date());
                    return purchaseEntity;
                }).collect(Collectors.toList());
        //2.更改采购项的状态
        this.updateBatchById(purchaseEntities);
        purchaseEntities.forEach(purchaseEntity -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(purchaseEntity.getId());
            List<PurchaseDetailEntity> entities = purchaseDetailEntities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(entities);
        });

    }

    //todo:在purchase_detail中添加失败的原因，预期采购数量，实际采购数量
    @Transactional
    @Override
    public void donePurchases(PurchaseDoneVo doneVo) {
        //1.改变采购单的状态
        Long id = doneVo.getId();
        //2.改变采购项的状态
        boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo purchaseItemDoneVo : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseItemDoneVo.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将成功采购的采购项入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(purchaseItemDoneVo.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
            }
            purchaseDetailEntity.setId(purchaseItemDoneVo.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}