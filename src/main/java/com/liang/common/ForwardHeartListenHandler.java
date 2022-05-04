package com.liang.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description: 服务端或客户端在转发消息时使用的心跳监听，用于侦测监听的端口多久没有进行数据收发了，
 * 太久没进行数据收发的可能原因是socket连接异常断开了但仍然显示为Established状态，程序阻塞在了read上
 * @Author: LiangYang
 * @Date: 2022/5/4 上午10:55
 **/
@Slf4j
public class ForwardHeartListenHandler implements Runnable{
    private final String endpointName;
    private final Socket socket;
    private final AtomicLong lastTime;

    public ForwardHeartListenHandler(String endpointName, Socket socket, AtomicLong lastTime) {
        this.endpointName = endpointName;
        this.socket = socket;
        this.lastTime = lastTime;
    }

    @Override
    public void run() {
        while (socket.isConnected() && !socket.isClosed()) {
            try {
                Thread.sleep(HeartbeatUtil.listenFreeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            long currentTime = System.currentTimeMillis();
            long dataFreeMills = currentTime - lastTime.get();
            log.trace("{}\t数据收发空闲间隔需要在{}内，检测到间隔为{}", endpointName, HeartbeatUtil.dataFreeMillsMax, dataFreeMills);
            if (dataFreeMills > HeartbeatUtil.dataFreeMillsMax) {
                log.warn("{}\t数据收发空闲间隔过久,将关闭连接{}", endpointName, socket);
                break;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
