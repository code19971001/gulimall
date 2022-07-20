package com.it.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.it.common.constant.LoggerConstant;
import com.it.gulimall.order.config.AlipayTemplate;
import com.it.gulimall.order.service.OrderService;
import com.it.gulimall.order.vo.OrderConfirmVo;
import com.it.gulimall.order.vo.OrderSubmitVo;
import com.it.gulimall.order.vo.PayVo;
import com.it.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author : code1997
 * @date : 2021/7/13 20:56
 */
@Controller
@Slf4j
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 创建防止重复令牌。
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 封装订单提交信息：参照京东，无需提交需要购买的商品，去购物车再次获取一遍。
     * 下单，创建订单，验证令牌，价格，锁库存。
     * 下单成功之后来到支付页面
     * 下单失败回到订单确认页，重新确认订单信息。
     * 分布式事务：因为我们锁定库存失败了，但是我们订单和订单项却添加成功了，因此需要进行修改。
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
        log.info(LoggerConstant.LOGGER_PREFIX + "submitOrder::" + responseVo);
        if (responseVo.getCode() == 0) {
            //代表下单成功，进入到终止符选择页面
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            String message = "下单失败：";
            switch (responseVo.getCode()) {
                case 1:
                    message += "订单信息过期，请重新再次提交。";
                    break;
                case 2:
                    message += "订单价格发生变化，请确认后再次提交。";
                    break;
                case 3:
                    message += "商品库存不足。";
                    break;
                default:
                    message += "出现未知错误";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", message);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }


    @ResponseBody
    @GetMapping("/payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        return alipayTemplate.pay(payVo);
    }


}
