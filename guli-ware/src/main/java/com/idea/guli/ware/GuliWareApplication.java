package com.idea.guli.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@EnableTransactionManagement
//@MapperScan("com.idea.guli.ware.dao")
//在mybatisconfig中配置事务和dao扫描MapperScan


//@EnableFeignClients(basePackages="com.idea.guli.ware.feign") //配置在主类中可以简写

@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@EnableTransactionManagement
@SpringBootApplication
public class GuliWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuliWareApplication.class, args);

    }

}
