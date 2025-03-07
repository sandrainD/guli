package com.idea.guli.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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


本地事务失效问题
同一个对象内事务方法互调默认失效，原因 绕过了代理对象，事务使用代理对象来控制的
解决:使用代理对象来调用事务方法

1 ) 引入aop-starter;spring-boot-starter-aop;引入了aspectj
2 ) @EnableAspectIAutoProxy;开启 aspectj 动态代理功能。以后所有的动态代理都是aspectj创建的。(即使没有接口也可以创建动态代理
        (exposeProxy = true)对外暴露代理对象
3 ) 本类互调调用对象       OrderServiceImpl orderservice=(OrderServiceImpl)AopContext.currentProxy();




seata分布式事务

1每一个微服务先必须创建 undo_log;
2安装事务协调器;seata-server:https://github.com/seata/seata/releases
3整合
    1、导入依赖 spring-cloud-starter-alibaba-seata seata-all-0.7.1
    2、解压并启动seata-server;
        registry.conf:注册中心配置;修改registry type=nacos
        file.conf:
    3、所有想要用到分布式事务的微服务使用seata DatasourceProxy代理自己的数据源
    4、每个微服务，都必须导入registry.conf file.conf
    5、启动测试分布式事务
    6、给分布式大事务的入口标注@Globalrransactional
    7、每一个远程的小事务用 @Transactional

 */
//本地事务，只能控制自己回滚，无法控制远程服务回滚
//分布式事务，最大原因，网络问题&本地方法异常，远程不回滚
//(isolation = Isolation.REPEATABLE_READ)MYSQL默认隔离级别-可重复读
//(propagation = Propagation.REQUIRED)事务的传播行为
//,timeout = 30超时时间30秒  如果a方法调用b方法，公用一个事务，a的事务超时时间会传播到b事务
//在同一个类里面，编写两个方法，内部调用的时候，会导致事务设置失效。原因是没有用到代理对象的缘故。
@EnableAspectJAutoProxy(exposeProxy = true)
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
