package com.it.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testMD5() {
        System.out.println(DigestUtils.md5Hex("12356"));
        //可以指定一个salt:需要在数据库中保存一个盐值
        System.out.println(Md5Crypt.md5Crypt("12356".getBytes(),"$12312$"));
        //spring不需要自动存储盐值字段
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
        System.out.println("matches==>"+encoder.matches("123456", ""));
    }

}
