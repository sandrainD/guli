package com.idea.guli.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.order.entity.OrderReturnApplyEntity;

import java.util.Map;

/**
 * 
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
public interface OrderReturnApplyService extends IService<OrderReturnApplyEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

