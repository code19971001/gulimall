package com.it.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.it.gulimall.order.vo.PayVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用于调用alipay的配置文件。具体功能参照demo以及官方文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.pay
 * todo:定时任务进行自动对账。
 * 收单功能：
 * 1、在支付页面，不支付，一直刷新，订单过期之后才支付，订单的状态已经改为已支付，但是库存已经解锁了==>使用支付宝的自动收单的功能。timeout_express参数设置。
 * 2、因为网络原因，订单解锁完成，正在解锁库存的时候，异步通知才到==>解锁订单的时候，手动调用alipay收单。
 * 3、其他原因：每天晚上闲时下载支付宝对账单，进行一一对账。
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
@Slf4j
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000117692693";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCipB/kWTxaBqTmouxc2oTQNMakf8A7CP+qmXs4Eq2uvgOMJtuZFCD/UtKTzAzHHjjk59DGdOWBN1JAdxWbmacdRjpGjHMdOf6AquLZfIFC3E7s6A8Zi8YydFyJTKd3/cwzyPnRuGSL/ojS07SyREDNI2AmwYhd6LFr+xy/TnZPzRHw/8NWxn3E7rUy2xMmnCLf2lKFLiz6n5nW194aZeVOVM0qAsUNHXkMdlCwdfhwBwxtT8unhANhEHmapZh6hmMU0yTxFITZ8vd/JiChtiOvTAF3XVdSXFokp0xoVN9slZmDTa0Trk+IeZZZNqDiwCCuZtH0SLjl6mbJLhv/lIJBAgMBAAECggEAQYDuo05ZYzzHz2gQ4DjWQvDujDQznNM7/V7BZM0JDJbU4O6JhTa5L3wQ+Zu2DcAbU/4VdMiB4rAAPZx4pGmXB9BQCjfo6P2BhhrNMKDXgrEuTHYOFtDXq1x4JIgt7sLWKokoLJIiVMvYGWlQnnekzboQsKtDm+brBj1lz5rvRaX/0XoUg5a7Ii6Z8rZXSa9LPoXUsmY1Qt2kxW0Ta55fBU7iLhLwIuHZda3ihsoIMa5cLk8DHfe3cNAIu+St7y2yW61I84LEUEuPkSXlAhKfXsl03Xk+onXVN0P/qjVIq7tbqwUzm360PgRzmADD+MoPK4/0b/jkZb4YWWlDAJ7nXQKBgQD3EI6SmCAv9eEpETGXoL/e++NM/nXj+GOOd1kIuK+JwM8P+i1p2oQYk5/3u8cPMihcPbG3FRcWhC+PynbvAwgMQJo1msI/MpPuqrWZJlqI5JNGh3nmHRHQxTVKRAMXnLXHMUUVFiELFPrsAOK1EBhgUJ9N45W1AVH+4wQW1dmzCwKBgQCohe8SQUBaT7hIrtXG5to/n/JLz69WI+Psse1VYTq6/gAap5fYKKhyCoeIcb5y4VZ/uWqQ3MS5fifJcvGSJHwpVAlnmDy5GRw/9e9NAhFxew6qPwAj0wI0v/JGmBNA5Eo+IPL9Bp4AGL77YkP++P6OX2XZUYXdGeyepNixENfvYwKBgEIBnWWS2P6XPTmAi3RMERbcyuRWE179jHOSdSn8VYGDDo7hMuLV63a5cgJ4m3z21IuYO/OC4fE1DZPn7pB6idrkI47wsra5p4Ya+3tYINXz41jzduDAoXLaTRtzQjQ7vODjy+YPTHKufz8wGWFwVGLfB0CC7gs2uBZVxlDnf0rzAoGAHLUUEKwE5l2GKvt9dzCEJSp9/+QVHjtm7D8zCsRRWOFD60Z5W10xtJuOkvOtWayBK6jOOnchG+gdpvdpp1eHvxKNt5P7ZBye71ZZuWiMyCQKT34D7BZZ3dqEzvnA7eD1tLewLOIfsCyG/BO1OY347w/ZLbfxccSkxjGWqDMSVbkCgYEAmP17azjOjXIcJTLmh/AQcsuBGPNSqblxt9lrgtqUoQTW34SMwUwz2xYrnv0OH8nrREgGbp1foabQeUS7GsfuatqxjaObQahHRAnbnl5M0vblFdbGevufdpRnDhSRN9bKAc2a53Y61D2XA8nqOXN+STKFoih7l6PeuRK9qNCxtww=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApFYemHCTCv1akn9rwEfqCQQTZDKavDK4ZbUxOWw+fFvIw06xmo7+CCPUyYcwQtmgJfKY0RouSfOv7GnhJuxjMSRAWschknopNSFX7Zw3uGu9WH/gNrNr+xLKzf66+juxTB0q5ISNlSa9pVcHJ7JmvBBUec7J4batiSYIelry7tLYfAH3kQRTiw+xbZW2ag7e8i86EWPVR4Lj2oyOOXERs5Kp3knDWptKKCs4qgKvYRmPtXR1K7QxsOCLqJLXfKYygg5MVw0+kilREb760i46yITngOEGOuORnhIdkuTRNcj0h67kc5uaGdZQDauVOSnea6m/faJLEB34dMrePEPrXQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 返回格式
    private String format = "json";

    private String timeout = "15m";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, format,
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        log.info("支付宝的响应：" + result);

        return result;

    }
}
