package com.idea.guli.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.idea.common.exception.NoStockException;
import com.idea.common.to.SkuHasStockVo;
import com.idea.common.to.mq.OrderTo;
import com.idea.common.to.mq.StockDetailTo;
import com.idea.common.to.mq.StockLockedTo;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.Query;
import com.idea.common.utils.R;
import com.idea.guli.ware.dao.WareSkuDao;
import com.idea.guli.ware.entity.WareOrderTaskDetailEntity;
import com.idea.guli.ware.entity.WareOrderTaskEntity;
import com.idea.guli.ware.entity.WareSkuEntity;
import com.idea.guli.ware.feign.OrderFeignService;
import com.idea.guli.ware.feign.ProductFeignService;
import com.idea.guli.ware.service.WareOrderTaskDetailService;
import com.idea.guli.ware.service.WareOrderTaskService;
import com.idea.guli.ware.service.WareSkuService;
import com.idea.guli.ware.vo.OrderItemVo;
import com.idea.guli.ware.vo.OrderVo;
import com.idea.guli.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    WareOrderTaskService orderService;

    @Autowired
    WareOrderTaskDetailService orderDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ProductFeignService productFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                StackTraceElement[] stackTrace = e.getStackTrace();
            }


            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(item -> {
            Long count = this.baseMapper.getSkuStock(item);
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(item);
            skuHasStockVo.setHasStock(count == null?false:count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    /**
     * 锁定库存
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //保存库存工作单详情,方便追溯
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderService.save(taskEntity);
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //找出那些仓库有货
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //仓库为空
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                //返回1成功(1行受影响),返回0失败(0行受影响)
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderDetailService.save(detailEntity);
                    StockLockedTo to = new StockLockedTo();
                    StockDetailTo detailTo = new StockDetailTo();
                    to.setId(taskEntity.getId());
                    BeanUtils.copyProperties(detailEntity, detailTo);
                    //防止回滚之后找不到数据,所以保存完整库存单
                    to.setDetail(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", to);
                    break;
                } else {
                    //当前仓库库存不足，尝试下一个仓库

                }
            }
            if (skuStocked == false) {
                //当前商品所有仓库都无货(没锁住库存)
                throw new NoStockException(skuId);
            }
        }
        return true;
    }
    //内部类
    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解锁库存
        //1.查询关于这个订单的锁定库存信息
        WareOrderTaskDetailEntity orderTaskDetailEntity = orderDetailService.getById(detailId);
        if (orderTaskDetailEntity != null) {
            //有,库存锁定成功，根据订单情况解锁
            Long id = to.getId();//库存工作单Id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = (OrderVo) r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //没有这个订单 或者 有订单但订单状态是已取消,解锁库存
                    //只有状态是1,才能解锁
                    if (orderTaskDetailEntity.getLockStatus() == 1) {
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //其它状态(包含订单成功)不解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //没有，库存锁定失败，库存回滚，这种情况无需解锁
        }
    }
    /*
     *防止因为订单服务故障,导致订单状态未改变，从而无法解锁库存
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询最新库存状态
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        List<WareOrderTaskDetailEntity> list = orderDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id)
                .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        //更新库存工作单状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        orderDetailService.updateById(entity);
    }
}