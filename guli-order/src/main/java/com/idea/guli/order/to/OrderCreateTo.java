package com.idea.guli.order.to;



import com.idea.guli.order.entity.OrderEntity;
import com.idea.guli.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> items;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
