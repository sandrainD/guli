package com.idea.guli.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * ֧
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
@Data
@TableName("oms_payment_info")
public class PaymentInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private String orderSn;
	/**
	 * 
	 */
	private Long orderId;
	/**
	 * ֧
	 */
	private String alipayTradeNo;
	/**
	 * ֧
	 */
	private BigDecimal totalAmount;
	/**
	 * 
	 */
	private String subject;
	/**
	 * ֧
	 */
	private String paymentStatus;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * ȷ
	 */
	private Date confirmTime;
	/**
	 * 
	 */
	private String callbackContent;
	/**
	 * 
	 */
	private Date callbackTime;

}
