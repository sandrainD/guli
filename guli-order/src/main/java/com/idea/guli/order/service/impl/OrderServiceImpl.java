package com.idea.guli.order.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.idea.common.to.mq.OrderTo;
import com.idea.common.to.mq.SeckillOrderTo;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.Query;
import com.idea.common.utils.R;
import com.idea.common.vo.MemberRespVo;
import com.idea.guli.order.constant.OrderConstant;
import com.idea.guli.order.dao.OrderDao;
import com.idea.guli.order.entity.OrderEntity;
import com.idea.guli.order.entity.OrderItemEntity;
import com.idea.guli.order.entity.PaymentInfoEntity;
import com.idea.guli.order.enume.OrderStatusEnum;
import com.idea.guli.order.feign.CartFeginService;
import com.idea.guli.order.feign.MemberFeginService;
import com.idea.guli.order.feign.ProductFeignService;
import com.idea.guli.order.feign.WmsFeignService;
import com.idea.guli.order.interceptor.LoginUserInterceptor;
import com.idea.guli.order.service.OrderItemService;
import com.idea.guli.order.service.OrderService;
import com.idea.guli.order.service.PaymentInfoService;
import com.idea.guli.order.to.OrderCreateTo;
import com.idea.guli.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    //将页面传递过来的数据在同一个线程内进行共享
    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    CartFeginService cartFeginService;

    @Autowired
    MemberFeginService memberFeginService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    PaymentInfoService paymentInfoService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 确认订单
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.远程客户地址列表
            //每个线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeginService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> getCartFuture = CompletableFuture.runAsync(() -> {
            //2.远程购物车所有选中的购物项
            //每个线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeginService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = (List<SkuStockVo>) hasStock.getData(new TypeReference<List<SkuStockVo>>() {});
            if (data != null) {
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(collect1);
            }
        }, executor);
        //3.查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);
        //4.防重复令牌提交
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setToken(token);
        CompletableFuture.allOf(getAddressFuture, getCartFuture).get();
        return confirmVo;
    }

    /**
     * 提交订单
     * @param vo
     * @return
     */
    @Transactional (propagation = Propagation.REQUIRED,timeout = 30)
//    @GlobalTransactional
    //本地事务，只能控制自己回滚，无法控制远程服务回滚
    //分布式事务，最大原因，网络问题&本地方法异常，远程不回滚
    //(isolation = Isolation.REPEATABLE_READ)MYSQL默认隔离级别-可重复读
    //(propagation = Propagation.REQUIRED)事务的传播行为
    //,timeout = 30超时时间30秒  如果a方法调用b方法，公用一个事务，a的事务超时时间会传播到b事务
    //在同一个类里面，编写两个方法，内部调用的时候，会导致事务设置失效。原因是没有用到代理对象的缘故。
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        responseVo.setCode(0);
        //1.验证令牌(令牌的对比和删除必须保证原子性)
        //如果调用get方法与传入的val相同，调用del方法，不相同返回0(0失败，1成功)
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //使用Lua脚本
        //Lua脚本里使用的是长度为1的List，所以需要asList
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        //String s = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        if (result == 0) {
            //失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //成功
            //1.创建订单
            OrderCreateTo order = createOrder();
            //2.验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //成功
                //3.保存订单
                saveOrder(order);
                //4.锁定库存,有异常回滚数据
                //订单号,skuId,skuName,数量
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //远程锁库存
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //成功(rebbitMQ消息通知)
                    responseVo.setOrder(order.getOrder());
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //失败
                    //throw new NoStockException(msg);
                    responseVo.setCode(3);
                    return responseVo;
                }
            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 创建订单
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        //执行创建订单方法
        OrderEntity orderEntity = buildOrder(orderSn);
        //得到所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //验价
        computePrice(orderEntity, itemEntities);
        orderCreateTo.setItems(itemEntities);
        orderCreateTo.setOrder(orderEntity);
        return orderCreateTo;
    }

    /**
     * 构建订单
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberRespVo.getId());
        //获取收货地址信息
        OrderSubmitVo submitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo data = (FareVo) fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费信息
        entity.setFreightAmount(data.getFare());
        //设置收货信息
        entity.setReceiverCity(data.getAddress().getCity());
        entity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        entity.setReceiverName(data.getAddress().getName());
        entity.setBillReceiverPhone(data.getAddress().getPhone());
        entity.setReceiverPostCode(data.getAddress().getPostCode());
        entity.setReceiverProvince(data.getAddress().getProvince());
        entity.setReceiverRegion(data.getAddress().getRegion());
        //设置订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        entity.setDeleteStatus(0);//删除状态,0未删除
        return entity;
    }

    /**
     * 构建所有订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //获取所有订单项,最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeginService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(cartItem -> {
                //构建订单项数据
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1.订单信息:订单号
        //2.商品spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = (SpuInfoVo) r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //3.商品sku信息
        itemEntity.setSkuId(skuId);
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4.优惠信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //5.积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().intValue() * cartItem.getCount());
        itemEntity.setGiftIntegration(cartItem.getPrice().intValue() * cartItem.getCount());
        //6.订单项价格信息
        //实际金额
        BigDecimal orgin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orgin.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }


    /**
     * 验价方法
     *
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        //订单总额,计算各种优惠的总额,计算积分和成长值总额
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal promotionAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        Integer integration = 0;
        Integer growth = 0;
        BigDecimal total = new BigDecimal("0.0");
        for (OrderItemEntity entity : itemEntities) {
            couponAmount = couponAmount.add(entity.getCouponAmount());
            promotionAmount = promotionAmount.add(entity.getPromotionAmount());
            integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
            growth += entity.getGiftGrowth();
            integration += entity.getGiftIntegration();
            total = total.add(entity.getRealAmount());
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setGrowth(growth);
        orderEntity.setIntegration(integration);
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity entity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return entity;
    }

    /**
     * 保存订单
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 关闭订单
     *
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        //查询订单最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关闭订单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                //TODO 每一条消息进行日志记录(数据库保存每一条消息的详细信息)
                //TODO 定期扫描数据库将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 将没法送成功的消息进行重试发送
            }
        }
    }

    /**
     * 获取订单的支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setOut_trade_no(order.getOrderSn());
        //支付宝仅支持小数点后两位,截取位数并向上取值
        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        OrderItemEntity entity = list.get(0);
        payVo.setSubject(entity.getSkuName());
        payVo.setBody(entity.getSkuAttrsVals());
        return payVo;
    }

    //订单分页查询
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
//            order.setItemEntities(list);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);
        return new PageUtils(page);
    }

    /**
     * 处理支付宝异步通知返回结果
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1.保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(infoEntity);
        //2.修改订单状态
        if(vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")){
            //支付成功
            String outTradeNo = vo.getOut_trade_no();
//            this.baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    /**
     * 创建秒杀订单
     * @param seckillOrder
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(new BigDecimal("" + seckillOrder.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);
        //TODO 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrder.getOrderSn());
        itemEntity.setRealAmount(multiply);
        //TODO 获取当前SKU详细信息进行设置 productFeignService.getSpuInfoBySkuId()
        itemEntity.setSkuQuantity(seckillOrder.getNum());
        orderItemService.save(itemEntity);
    }
}