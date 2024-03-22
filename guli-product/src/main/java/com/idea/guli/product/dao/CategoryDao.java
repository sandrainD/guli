package com.idea.guli.product.dao;

import com.idea.guli.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 15:49:09
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
