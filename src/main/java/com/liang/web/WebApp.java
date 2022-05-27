package com.liang.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午2:37
 **/
@SpringBootApplication
@MapperScan("com.liang.web.dao")
public class WebApp {
    public static void start(int port, String username, String password, String[] args){
        SpringApplication app = new SpringApplication(WebApp.class);
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", port);
        map.put("web.username", username);
        map.put("web.password", password);
        app.setDefaultProperties(map);
//        app.setDefaultProperties(Collections.singletonMap("server.port", port);
        app.run(args);
    }

}
