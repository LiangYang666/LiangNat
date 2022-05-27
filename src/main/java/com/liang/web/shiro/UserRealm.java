package com.liang.web.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Description: UserRealm 定义授权和认证
 * @Author: LiangYang
 * @Date: 2022/5/25 下午11:57
 **/
public class UserRealm extends AuthorizingRealm {
    @Value("${web.username}")
    private String username;
    @Value("${web.password}")
    private String password;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String usernameGet = (String) authenticationToken.getPrincipal();
        if (!usernameGet.equals(username)) {
            throw new AuthenticationException("用户名不正确");
        }
        return new SimpleAuthenticationInfo(usernameGet, password, getName());
    }
}
