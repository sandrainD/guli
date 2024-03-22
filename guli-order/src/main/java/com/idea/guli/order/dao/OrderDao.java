package com.idea.guli.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.idea.guli.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
