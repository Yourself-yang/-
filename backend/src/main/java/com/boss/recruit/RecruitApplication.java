package com.boss.recruit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线招聘求职平台后端启动类
 * 技术栈：SpringBoot 2.7 + MyBatis-Plus + H2(可切MySQL)
 * 微服务说明：本系统按微服务思想划分模块（user-service / job-service / chat-service / match-service），
 *           生产环境可拆分为独立服务并通过 Spring Cloud Gateway 统一网关、Nacos 注册中心进行治理。
 */
@SpringBootApplication
@MapperScan("com.boss.recruit.mapper")
public class RecruitApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecruitApplication.class, args);
        System.out.println("===== 在线招聘求职平台后端启动成功，端口 8080 =====");
    }
}
