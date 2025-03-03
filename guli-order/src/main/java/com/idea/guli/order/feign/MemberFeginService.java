package com.idea.guli.order.feign;


import com.idea.guli.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("guli-member")
public interface MemberFeginService {
    @GetMapping("member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
