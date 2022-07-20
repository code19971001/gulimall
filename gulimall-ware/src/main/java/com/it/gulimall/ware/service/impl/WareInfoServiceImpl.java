package com.it.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.common.utils.R;
import com.it.gulimall.ware.dao.WareInfoDao;
import com.it.gulimall.ware.entity.WareInfoEntity;
import com.it.gulimall.ware.feign.MemberFeignService;
import com.it.gulimall.ware.service.WareInfoService;
import com.it.gulimall.ware.vo.FireRespVo;
import com.it.gulimall.ware.vo.MemberReceiveAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key).or()
                    .like("name", key).or().
                    like("address", key).or().
                    like("aeracode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据用户的收货地址计算运费
     * 简单使用手机号的后两位进行运费计算。
     */
    @Override
    public FireRespVo getFare(Long addrId) {
        R addrInfo = memberFeignService.info(addrId);
        MemberReceiveAddressVo addrInfoData = addrInfo.getData("memberReceiveAddress", new TypeReference<MemberReceiveAddressVo>() {
        });
        if (addrInfoData != null) {
            FireRespVo fireRespVo = new FireRespVo();
            String phone = addrInfoData.getPhone();
            fireRespVo.setFire(new BigDecimal(phone.substring(phone.length() - 2)));
            fireRespVo.setMemberReceiveAddress(addrInfoData);
            return fireRespVo;
        }
        return null;
    }

}