package com.idea.guli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.product.entity.SpuInfoEntity;
import com.idea.guli.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 15:49:09
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);


    PageUtils queryPageByCondition(Map<String, Object> params);

    //商品上架
    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

