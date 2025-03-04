package com.idea.guli.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/*
使用RabbitMQ
1、引入amqp场景;RabbitAutoconfiguration 就会自动生效

2、给容器中自动配置了
    RabbitTemplate、AmgpAdmin、CachingConnectionFactory、RabbitMessagingTemplate;
    所有的属性都是 spring.rabbitmg
    @ConfigurationProperties(prefix="spring.rabbitmg"
    public class RabbitProperties

3、给配置文件中配置 spring.rabbitmg 信息

4、@EnableRabbit:@EnableXxxxx;开启功能
5、监听消息:使用@RabbitListener;必须有@EnableRabbit
    @RabbitListener:类+方法上(监听哪些队列即可
    @RabbitHandler:标在方法上(重载区分不同的消息)



 */

@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@EnableFeignClients
@SpringBootApplication
public class GuliOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuliOrderApplication.class, args);
    }

}
