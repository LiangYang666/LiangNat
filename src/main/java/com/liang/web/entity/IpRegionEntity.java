package com.liang.web.entity;

import lombok.Data;

@Data
public class IpRegionEntity {
    String country;     // 国家
    String region;      // 地区
    String province;    // 省份
    String city;        // 城市
    String isp;         // 运营商


    public IpRegionEntity(String ip2RegionStr){
        String[] splits = ip2RegionStr.split("\\|");
        if (splits.length!=5)   return;
        this.country = splits[0].equals("0")?null:splits[0];
        this.region = splits[1].equals("0")?null:splits[1];
        this.province = splits[2].equals("0")?null:splits[2];
        this.city = splits[3].equals("0")?null:splits[3];
        this.isp = splits[4].equals("0")?null:splits[4];
    }

}
