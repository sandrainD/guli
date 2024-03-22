package com.idea.guli.member.feign;

import com.idea.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("guli-coupon")
public interface CouponFeign {
    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();

}
