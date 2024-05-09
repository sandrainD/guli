package com.idea.guli.product.dao;

import com.idea.guli.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 15:49:10
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selselectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
