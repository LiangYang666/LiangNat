package com.liang.web.shiro;

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 上午12:04
 **/
@Configuration
public class ShiroConfig {
    //  1.定义userRealm进springboot组件
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        userRealm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return userRealm;
    }
    //2. 获取安全管理器
    @Bean(name = "defaultWebSecurityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(
            @Qualifier("userRealm") UserRealm userRealm){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);    //注入
        return securityManager;
    }
    // 3. 过滤器设置
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            @Qualifier("defaultWebSecurityManager") DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(defaultWebSecurityManager); // 设置安全管理器
        //传入未登录用户访问登陆用户的权限所跳转的页面
        bean.setLoginUrl("/login");

        //访问未授权网页所跳转的页面
        bean.setUnauthorizedUrl("/unauthorized");
        Map<String, String> map = new LinkedHashMap<>();
        //允许未认证访问  需要设置login为anon 否则登陆成功后无法成功跳转。
        map.put("/login", "anon");
        map.put("/doLogin", "anon");
        map.put("/css/*.css", "anon");
        map.put("/js/*.js", "anon");

        //需要认证才能访问
        map.put("/**", "authc");
        bean.setFilterChainDefinitionMap(map);
        return bean;
    }

}
