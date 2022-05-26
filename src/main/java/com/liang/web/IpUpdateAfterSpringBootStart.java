package com.liang.web;

import com.liang.server.AllowedIpUtil;
import com.liang.web.dao.IpDao;
import com.liang.web.entity.IpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午3:58
 **/
@Component
@Order(value = 1)
public class IpUpdateAfterSpringBootStart implements ApplicationRunner {
    @Autowired
    IpDao ipDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<IpEntity> ipEntities = ipDao.selectList(null);
        for (IpEntity ipEntity : ipEntities) {
            AllowedIpUtil.ipSets.add(ipEntity.getIp());
        }
    }
}
