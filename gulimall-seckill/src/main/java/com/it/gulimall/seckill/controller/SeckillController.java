package com.it.gulimall.seckill.controller;

import com.it.common.utils.R;
import com.it.gulimall.seckill.service.SeckillService;
import com.it.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * todo:全局时区问题的考虑。
 *
 * @author : code1997
 * @date : 2021/7/28 23:15
 */
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/curSeckillSkus")
    @ResponseBody
    public R getCurSeckillSkus() {
        List<SeckillSkuRedisTo> seckillSkus = seckillService.getCurSeckillSkus();
        return R.ok().setData(seckillSkus);
    }

    @GetMapping("/sku/SeckillInfo/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo skuSeckillInfo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(skuSeckillInfo);
    }


    /**
     * url:http://seckill.gulimall.com/kill?killId=" + sessionId + "_" + skuId + "&num=" + number + "&code=" + code;
     * 只有登陆才可以进行秒杀行动
     * todo:
     * 1）上架秒杀商品都存在过期时间。
     * 2）秒杀后续流程，比如收货地址等信息的计算。
     */
    @GetMapping("/kill")
    public String seckillSku(@RequestParam("killId") String skillId, @RequestParam("code") String code, @RequestParam("num") Integer num, Model model) {
        String orderSn = seckillService.kill(skillId, num, code);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
