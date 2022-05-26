package com.liang.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/25 上午10:26
 **/
@RestController
public class HelloWorldController {
    @RequestMapping("/")
    public String sayHello(){
        return "Hello World!";
    }
}
