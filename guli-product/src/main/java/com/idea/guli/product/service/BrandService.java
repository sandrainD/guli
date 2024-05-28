package com.idea.guli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 15:49:09
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDeatil(BrandEntity brand);

    List<BrandEntity> getBeandsByIds(List<Long> brandIds);
}

