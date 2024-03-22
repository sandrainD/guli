package com.idea.guli.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.idea.common.utils.PageUtils;
import com.idea.guli.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:20:48
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

