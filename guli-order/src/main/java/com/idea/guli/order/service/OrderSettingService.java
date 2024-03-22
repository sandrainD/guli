package com.idea.guli.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.order.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

