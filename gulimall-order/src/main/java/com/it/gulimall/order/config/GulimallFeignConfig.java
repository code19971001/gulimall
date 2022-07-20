package com.it.gulimall.order.config;

import com.it.common.constant.LoggerConstant;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : code1997
 * @date : 2021/7/14 0:14
 */
@Configuration
@Slf4j
public class GulimallFeignConfig {

    /**
     * feign远程调用会构造请求，但是默认的requestTemplate并没有帮我们携带请求头信息，会导致session丢失，在构造的过程中存在很多请求拦截，因此我们需要自定义。
     */
    @Bean
    public RequestInterceptor requestInterceptor() {

        return (requestTemplate) -> {
            log.info(LoggerConstant.LOGGER_PREFIX + "feign正在进行Request的构造");
            //本质是使用ThreadLocal将请求的信息保存下来，以供其他的类来使用。
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest oldReq = requestAttributes.getRequest();
                requestTemplate.header("Cookie", oldReq.getHeader("Cookie"));
            }
        };
    }
}
