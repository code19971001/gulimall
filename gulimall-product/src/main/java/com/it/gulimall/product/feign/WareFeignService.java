package com.it.gulimall.product.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/20 21:34
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 远程调用返回的数据我们需要进行转换，为了解决这个问题
     * 1、R设计的时候可以加上泛型。 -->本次使用
     * 2、直接返回我们想要的数据。
     * 3、自己封装解析的结果。
     * 执行过程：
     * 1、构造请求数据，将对象转化为json
     *    RequestTemplate template = buildTemplateFromArgs.create(args);
     * 2、发送请求进行执行(执行成功会解码，响应数据)
     *    executeAndDecode(Template)
     *
     * 3、如果失败会进行retry，默认关闭。
     *   try{
     *       executeAndDecode(template)
     *   }catch(){
     *       try{retryer.continueOrPropagate(e);}
     *       catch(e){
     *           throw ex;
     *           continue;
     *       }
     *   }
     */
    @PostMapping("/ware/waresku/hasstock")
    R hasStock(@RequestBody List<Long> skuIds);
}
