package com.liang.common;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description: 心跳检测工具类，主要定义最后监测到包的时间和 查询间隔等
 * @Author: LiangYang
 * @Date: 2022/5/3 下午1:06
 **/
public class HeartbeatUtil implements Serializable {
    private static final long serialVersionUID = 6064454759002593196L;
    public static transient long delayMillsMax = 10000;        // 允许心跳检测两包之间的延迟最大时间
    public static transient long sendInterval = delayMillsMax/3;    // 发送心跳包的间隔时间
    public static transient long listenInterval = sendInterval;     // 监听心跳包的间隔时间

    private final AtomicLong lastHeartbeatTime = new AtomicLong();

    public HeartbeatUtil() {
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime.get();
    }

    public void setLastHeartbeatTime(long time) {
        this.lastHeartbeatTime.set(time);
    }

}
