package com.idea.guli.guligateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient //开启服务注册发现
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GuliGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuliGatewayApplication.class, args);
    }

}
