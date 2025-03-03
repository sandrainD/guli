package com.idea.guli.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.idea.common.utils.PageUtils;
import com.idea.common.utils.Query;
import com.idea.guli.order.dao.OrderItemDao;
import com.idea.guli.order.entity.OrderEntity;
import com.idea.guli.order.entity.OrderItemEntity;
import com.idea.guli.order.entity.OrderReturnReasonEntity;
import com.idea.guli.order.service.OrderItemService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

//@RabbitListener
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /*
        queues:声明需要监听的所有队列
        org.springframework.amgp.core.Message

        参数可以写一下类型
        1、Message message:原生消息详细信息。头+体
        2、T<发送的消息的类型>OrderReturnReasonEntity content;
        3、Channel channel:当前传输数据的通道

        Queue:可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一个收到此消息
        场景:
        1)、订单服务启动多个;同一个消息，只能有一个客户端收到
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void recieveMessage(Message message){
        System.out.println("接收到消息。。。内容："+message);
    }


    @RabbitHandler
    public void reciveMessage(Message msg, OrderReturnReasonEntity content, Channel channel){
        //消息头
        MessageProperties properties = msg.getMessageProperties();
        //消息体
        byte[] body = msg.getBody();
        //System.out.println("接收到消息...内容:"+msg+"==>类型"+content);
        //System.out.println("消息头:"+properties);
        //System.out.println("消息体:"+body);
        //Channel内按顺序自增
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        //签收消息,multiple是否批量签收消息;拒签消息,requeue=true发回服务器,服务器重新入队,false丢弃消息
        try {
            if(deliveryTag%2==0){
                channel.basicAck(deliveryTag,false);
                System.out.println("签收了消息..."+deliveryTag);
            }else {
             channel.basicNack(deliveryTag,false,false);
                System.out.println("拒签了消息..."+deliveryTag);
            }
        }catch (Exception e){
            //网络中断
        }

        System.out.println("接收到消息...内容:"+content);
    }
    @RabbitHandler
    public void reciveMessage2(OrderEntity content){
        System.out.println("接收到消息...内容:"+content);
    }
}