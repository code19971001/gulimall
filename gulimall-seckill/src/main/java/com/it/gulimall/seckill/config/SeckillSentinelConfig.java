package com.it.gulimall.seckill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.it.common.exception.BizCodeEnume;
import com.it.common.utils.R;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * 自定义流控之后的响应
 * @author : code1997
 * @date : 2021/8/4 23:55
 */
@Configuration
public class SeckillSentinelConfig {

    public SeckillSentinelConfig() {
        WebCallbackManager.setUrlBlockHandler((httpServletRequest, httpServletResponse, e) -> {
            R error = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            httpServletResponse.getWriter().write(JSON.toJSONString(error));
        });
    }

}
