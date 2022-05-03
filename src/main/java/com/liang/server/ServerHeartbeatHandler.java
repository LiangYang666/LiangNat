package com.liang.server;

import com.liang.common.AESUtil;
import com.liang.common.HeartbeatUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: 定时发送心跳包给客户端
 * @Author: LiangYang
 * @Date: 2022/5/3 下午12:52
 **/
@Slf4j
public class ServerHeartbeatHandler implements Runnable{
    private final Socket server2clientSocket;
    private final AESUtil aesUtil;
    public  ServerHeartbeatHandler(Socket server2clientSocket){
        this.server2clientSocket = server2clientSocket;
        aesUtil = new AESUtil();
    }
    @Override
    public void run() {
        byte[] bytes = "heart".getBytes();
        byte[] encryptBytes = aesUtil.encrypt(bytes);
        try {
            while (true){
                Thread.sleep(HeartbeatUtil.sendInterval);
                OutputStream out = server2clientSocket.getOutputStream();
                HeartbeatUtil pac = new HeartbeatUtil();
                synchronized (out){
                    out.write(MessageFlag.eventHeartbeat);
                    out.write(encryptBytes);
                }
                log.trace("Server\t发送心跳包：{}", pac);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        // 关闭连接
        try {
            server2clientSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
