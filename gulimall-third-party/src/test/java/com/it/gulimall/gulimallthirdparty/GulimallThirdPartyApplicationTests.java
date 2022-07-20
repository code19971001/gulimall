package com.it.gulimall.gulimallthirdparty;

import com.aliyun.oss.OSSClient;
import com.it.common.utils.HttpUtils;
import com.it.gulimall.gulimallthirdparty.component.SmsComponent;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void contextLoads() {
    }
    @Test
    public void testSmsComponent(){
        smsComponent.sendSms("15137928972", "123456");
    }
    @Test
    public void testSms(){
        String host = "https://aliapi.market.alicloudapi.com";
        String path = "/smsApi/verifyCode/send";
        String method = "POST";
        String appcode = "879ee4ad5f674ff6bab23df3cee8f646";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("phone", "15137928972");
        querys.put("templateId", "540");
        querys.put("variables", "123456");
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

    @Test
    public void testUpload1() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("E:\\12_zygy\\house\\timg.jpg");
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("code1997-gulimall", "test.jpg", inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成！");
    }

}
