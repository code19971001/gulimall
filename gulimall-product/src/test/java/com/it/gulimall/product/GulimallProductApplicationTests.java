package com.it.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.it.gulimall.product.dao.AttrGroupDao;
import com.it.gulimall.product.dao.SkuSaleAttrValueDao;
import com.it.gulimall.product.entity.BrandEntity;
import com.it.gulimall.product.service.BrandService;
import com.it.gulimall.product.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService service;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void testGetSaleAttrsBySpuId() {
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(14L));
    }

    @Test
    public void testGetAttrGroupWithAttrsBySpuId() {
        attrGroupDao.getAttrGroupWithAttrsBySpuId(14L, 225L).forEach(System.out::println);
    }

    @Test
    public void contextLoads2() {

        List<BrandEntity> list = service.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        list.forEach(System.out::println);
    }

/*    @Test
    public void testUpload() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tC3ZUuG35YorrCpiJEz";
        String accessKeySecret = "scRzyxGDPhM4r4YABxsY9yQR204RZU";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("E:\\12_zygy\\house\\timg.jpg");
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("code1997-gulimall", "test.jpg", inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成！");
    }*/

    @Test
    public void testGetPath() {
        Long[] catelogPath = categoryService.findCatelogPath(255L);
        System.out.println(Arrays.toString(catelogPath));
    }

    @Test
    public void testRedis() {
        stringRedisTemplate.opsForValue().set("admin", "18");
        String admin = stringRedisTemplate.opsForValue().get("admin");
        System.out.println(admin);
    }

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }

}
