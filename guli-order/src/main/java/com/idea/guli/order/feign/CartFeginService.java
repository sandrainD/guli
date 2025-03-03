package com.idea.guli.order.feign;


import com.idea.guli.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("guli-cart")
public interface CartFeginService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
