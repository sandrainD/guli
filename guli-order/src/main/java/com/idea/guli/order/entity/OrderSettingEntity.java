package com.idea.guli.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
@Data
@TableName("oms_order_setting")
public class OrderSettingEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Integer flashOrderOvertime;
	/**
	 * 
	 */
	private Integer normalOrderOvertime;
	/**
	 * 
	 */
	private Integer confirmOvertime;
	/**
	 * 
	 */
	private Integer finishOvertime;
	/**
	 * 
	 */
	private Integer commentOvertime;
	/**
	 * 
	 */
	private Integer memberLevel;

}
