package com.liang.client;

import com.liang.common.HeartbeatUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * @Description: 心跳检测客户端处理线程
 * @Author: LiangYang
 * @Date: 2022/5/3 下午2:48
 **/
@Slf4j
public class ClientHeartbeatHandler implements Runnable{
    private final HeartbeatUtil heartbeatUtil;
    private final Socket client2serverSocket;

    public ClientHeartbeatHandler(Socket client2serverSocket, HeartbeatUtil heartbeatUtil) {
        this.heartbeatUtil = heartbeatUtil;
        this.client2serverSocket = client2serverSocket;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(HeartbeatUtil.listenInterval);
                long delayMills = System.currentTimeMillis() - heartbeatUtil.getLastHeartbeatTime();
                log.trace("Client\t延时间隔需要在{}内，检测到间隔为{}", HeartbeatUtil.delayMillsMax, delayMills);
                if (delayMills>HeartbeatUtil.delayMillsMax){
                    log.warn("Client\t心跳检测时间超时,，将关闭连接{}", client2serverSocket);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            client2serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
