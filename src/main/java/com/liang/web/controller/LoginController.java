package com.liang.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description: 登录
 * @Author: LiangYang
 * @Date: 2022/5/26 上午12:20
 **/
@Controller
public class LoginController {
    @Value("${web.username}")   // 取配置中的username，未在application中定义，在WebApp.start中临时配置
    private String username;
    //如果需要使用shiro长期登陆，设置subject的rememberMe属性并且设置允许的范围为user。authc不允许被rememberMe用户访问。
    //这就是我们传入账号密码测试的地方
    @PostMapping(value = "/doLogin")
    public String doLogin(@RequestParam(value = "username") String username,
                        @RequestParam(value = "password") String password){
        Subject subject = SecurityUtils.getSubject();
        try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            subject.login(usernamePasswordToken);
            System.out.println("登陆成功");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("登陆失败");
        }
        return "redirect:/index";
    }

    @GetMapping(value = "/login")
    public String login(Model model){
        model.addAttribute("username", username);
        return "login";
    }
    @GetMapping("/logout")
    public String logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/login";
    }

}
