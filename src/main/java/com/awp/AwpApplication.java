package com.awp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AWP（记账网页）后端启动类。
 */
@SpringBootApplication
@MapperScan("com.awp.mapper")
public class AwpApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwpApplication.class, args);
    }
}
