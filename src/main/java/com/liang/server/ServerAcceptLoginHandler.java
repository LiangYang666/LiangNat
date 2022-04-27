package com.liang.server;

import com.liang.common.ByteUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static com.liang.common.MessageFlag.eventLogin;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:44
 **/

@Slf4j
class ServerAcceptLoginHandler implements Runnable{
    ConcurrentHashMap<Integer, Socket> serverPortMap;
    Socket clientSocket;

    public ServerAcceptLoginHandler(Socket clientSocket, ConcurrentHashMap<Integer, Socket> serverPortMap) {
        this.clientSocket = clientSocket;
        this.serverPortMap = serverPortMap;
    }

    @Override
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            int eventIndex = in.read();
            log.info("Server\t事件索引： "+ eventIndex);
            if (eventIndex == eventLogin){  // 登录事件 第2、3、4、5个字节放置监听端口数量
                int read;
                byte[] countBytes = new byte[4];
                read = in.read(countBytes);
                if (read==-1){
                    log.info("Server\t事件异常");
                    return;
                }
                int count = ByteUtil.byteArrayToInt(countBytes);
                byte[] portsBytes = new byte[count * 4];
                read = in.read(portsBytes);
                if (read != count * 4){
                    log.info("Server\t事件异常");
                    return;
                }
                for (int i = 0; i < count; i++) {
                    int port = ByteUtil.byteArrayToInt(portsBytes, i*4);
                    log.info("Server\t注册并监听映射端口: "+port);
//                    ServerSocket serverSocket = new ServerSocket(port);
                    serverPortMap.put(port, clientSocket);
                    log.info("Server\t成功监听端口: "+port);
                }
            } else{
                System.out.println("Server\t非正常事件");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

