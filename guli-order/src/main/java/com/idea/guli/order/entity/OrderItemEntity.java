package com.idea.guli.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * 
 * @author lts
 * @email j2568095536@gmail.com
 * @date 2022-10-09 17:44:26
 */
@Data
@TableName("oms_order_item")
public class OrderItemEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * order_id
	 */
	private Long orderId;
	/**
	 * order_sn
	 */
	private String orderSn;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * spu_name
	 */
	private String spuName;
	/**
	 * spu_pic
	 */
	private String spuPic;
	/**
	 * Ʒ
	 */
	private String spuBrand;
	/**
	 * 
	 */
	private Long categoryId;
	/**
	 * 
	 */
	private Long skuId;
	/**
	 * 
	 */
	private String skuName;
	/**
	 * 
	 */
	private String skuPic;
	/**
	 * 
	 */
	private BigDecimal skuPrice;
	/**
	 * 
	 */
	private Integer skuQuantity;
	/**
	 * 
	 */
	private String skuAttrsVals;
	/**
	 * 
	 */
	private BigDecimal promotionAmount;
	/**
	 * 
	 */
	private BigDecimal couponAmount;
	/**
	 * 
	 */
	private BigDecimal integrationAmount;
	/**
	 * 
	 */
	private BigDecimal realAmount;
	/**
	 * 
	 */
	private Integer giftIntegration;
	/**
	 * 
	 */
	private Integer giftGrowth;

}
