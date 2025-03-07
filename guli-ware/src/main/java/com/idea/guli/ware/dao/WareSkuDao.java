package com.idea.guli.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.idea.guli.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 18:02:12
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("item") Long item);

    List<Long> listWareIdHasSkuStock(Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);

    void unlockStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);

}
