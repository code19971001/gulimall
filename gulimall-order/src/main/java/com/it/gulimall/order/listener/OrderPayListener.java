package com.it.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.it.gulimall.order.config.AlipayTemplate;
import com.it.gulimall.order.service.OrderService;
import com.it.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 用于支付宝的异步回调：https://opendocs.alipay.com/open/203/105286
 *
 * @author : code1997
 * @date : 2021/7/26 18:03
 */
@RestController
public class OrderPayListener {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 只要我们返回success，支付宝就不会再给我们发送通知,否则支付宝会多次给我们发送异步回调。
     * 分布式事务中的柔性事务：最大努力通知型
     */
    @PostMapping("/payed/notify")
    public String payConfirm(PayAsyncVo payAsyncVo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //需要验证签名,才能进行下一步操作,防止数据被篡改伪造。
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决：如果出现乱码，使用如下代码来解决
            //valueStr = new String(valueStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            params.put(name, valueStr);
        }
        //计算得出通知验证结果
        boolean verifyResult = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type());
        if (verifyResult) {
            System.out.println("签名验证成功--");
            return orderService.handlePayResult(payAsyncVo);
        } else
            System.out.println("签名验证失败--");
            return "false";
    }


}
