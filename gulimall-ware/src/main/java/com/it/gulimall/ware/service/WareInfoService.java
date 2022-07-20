package com.it.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.common.utils.PageUtils;
import com.it.gulimall.ware.entity.WareInfoEntity;
import com.it.gulimall.ware.vo.FireRespVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FireRespVo getFare(Long addrId);

}

