package com.it.gulimall.order.feign;

import com.it.gulimall.order.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/13 22:48
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/{memberId}/addresses")
    List<MemberReceiveAddressVo> getMemberReceiveAddress(@PathVariable("memberId") Long memberId);

}
