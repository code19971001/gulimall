package com.it.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.to.MemberPrice;
import com.it.common.to.SkuReductionTo;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.gulimall.coupon.dao.SkuFullReductionDao;
import com.it.gulimall.coupon.entity.MemberPriceEntity;
import com.it.gulimall.coupon.entity.SkuFullReductionEntity;
import com.it.gulimall.coupon.entity.SkuLadderEntity;
import com.it.gulimall.coupon.service.MemberPriceService;
import com.it.gulimall.coupon.service.SkuFullReductionService;
import com.it.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    SkuFullReductionService skuFullReductionService;

    @Autowired
    MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1.保存满减打折信息，会员价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        //会员价格在下订单的时候进行计算
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        //2.sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
            //????
            //skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
            this.save(skuFullReductionEntity);
        }


        //3.计算会员价格
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntitys = memberPrice.stream().map(memberLevel -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(memberLevel.getId());
            memberPriceEntity.setMemberLevelName(memberLevel.getName());
            memberPriceEntity.setMemberPrice(memberLevel.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;

        }).filter(memberPriceEntity -> memberPriceEntity.getMemberPrice().compareTo(new BigDecimal(0)) > 0
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntitys);
    }

}