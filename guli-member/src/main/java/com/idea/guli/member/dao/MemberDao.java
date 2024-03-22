package com.idea.guli.member.dao;

import com.idea.guli.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:20:48
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
