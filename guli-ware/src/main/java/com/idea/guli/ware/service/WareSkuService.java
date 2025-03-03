package com.idea.guli.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.to.SkuHasStockVo;
import com.idea.common.utils.PageUtils;
import com.idea.guli.ware.entity.WareSkuEntity;
import com.idea.guli.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 18:02:12
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    boolean orderLockStock(WareSkuLockVo vo);
}

