package com.liang.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午2:37
 **/
@SpringBootApplication
@MapperScan("com.liang.web.dao")
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}
