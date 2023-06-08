package com.myy.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 启动类
 */
@EnableScheduling
@SpringBootApplication
@EnableSwagger2WebMvc
@MapperScan("com.myy.usercenter.mapper")
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

}
