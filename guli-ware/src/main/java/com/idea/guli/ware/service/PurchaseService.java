package com.idea.guli.ware.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.ware.entity.PurchaseEntity;
import com.idea.guli.ware.vo.MergeVo;
import com.idea.guli.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);


    void mergePurchase(MergeVo mergeVo);


    void received(List<Long> ids);


    void done(PurchaseDoneVo doneVo);


}

