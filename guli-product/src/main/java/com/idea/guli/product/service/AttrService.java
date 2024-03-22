package com.idea.guli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.product.entity.AttrEntity;
import com.idea.guli.product.vo.AttrGroupRelationVo;
import com.idea.guli.product.vo.AttrRespVo;
import com.idea.guli.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 15:49:10
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo... vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);
}

