package com.liang.server;



import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 上午9:24
 **/
@Slf4j
public class NatServer {

    public static void main(String[] args) throws IOException {

        int port = 10101;
        ServerSocket serverSocket = new ServerSocket(port);
        log.info("Server\t服务启动，开始监听端口 "+ port);

        while (true){
            Socket wanSocket = serverSocket.accept();
            log.info("Server\t接收到登录请求: "+ wanSocket);
            new Thread(new ServerGetEventHandler(wanSocket)).start();   // 开启事件监听
        }
    }
}
