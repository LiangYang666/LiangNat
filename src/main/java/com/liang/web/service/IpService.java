package com.liang.web.service;

import com.liang.web.dao.IpDao;
import com.liang.web.entity.IpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/25 下午7:54
 **/
@Service
public class IpService {
    @Autowired
    private IpDao ipDao;

    public List<IpEntity> getAllIp(){
        return ipDao.selectList(null);
    }

    public void saveIp(IpEntity ipEntity){
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ipEntity.setCreateTime(time);
        ipDao.insert(ipEntity);
    }
    public void deleteByIp(String ip){
        ipDao.deleteById(ip);
    }
    public IpEntity getByIp(String ip){
        return ipDao.selectById(ip);
    }

    public void updateIp(IpEntity allowedIp) {
        ipDao.updateById(allowedIp);
    }
}
