package com.liang.web.service;

import com.liang.web.entity.IpRegionEntity;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

@Service
public class Ip2RegionService {
    private final Searcher searcher;

    public Searcher initSearch(){
        Searcher searcher;
        ClassPathResource classPathResource = new ClassPathResource("/static/ip2region.xdb");
        try {
            InputStream inputStream = classPathResource.getInputStream();
            byte[] cBuff = StreamUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searcher;
    }


    public Ip2RegionService() {
        searcher = initSearch();
    }

    public IpRegionEntity getRegion(String ip) throws Exception {
        String rs = searcher.search(ip);
        return new IpRegionEntity(rs);
    }
}
