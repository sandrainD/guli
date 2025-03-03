package com.idea.guli.order.vo;

import com.idea.guli.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    private Integer code;//0成功


}
