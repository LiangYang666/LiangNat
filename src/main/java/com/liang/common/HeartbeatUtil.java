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

    public static transient long sendInterval = 7000;    // 发送心跳包的间隔时间 服务端将间隔该段时间定期发送心跳包给客户端 发送失败将关闭连接

    public static transient long listenInterval = sendInterval;     // 监听心跳包的间隔时间 客户端定期监听心跳包
    public static transient long delayMillsMax = sendInterval*3;      // 客户端允许心跳检测两包之间的延迟最大时间 超过这段时间还没监测到新的心跳包发送将认为该连接意外断开，关闭连接

    public static transient long listenFreeInterval = 2*60*1000;     // 监听查看多久没有收发数据了的间隔时间
    public static transient long dataFreeMillsMax = listenFreeInterval*2;     // 允许转发消息时数据活跃间隔的最大时间 超过该时间间隔还没有数据传输将认为该连接意外断开。将关闭该连接

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
