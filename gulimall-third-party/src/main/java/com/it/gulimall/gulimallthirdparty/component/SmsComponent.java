package com.it.gulimall.gulimallthirdparty.component;

import com.it.common.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : code1997
 * @date : 2021/6/22 23:29
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.mysms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String templateId = "540";
    private String appCode;

    public void sendSms(String telNumber, String code) {
        String method = "POST";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<>();
        querys.put("phone", telNumber);
        querys.put("templateId", templateId);
        querys.put("variables", code);
        Map<String, String> bodys = new HashMap<>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
