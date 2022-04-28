package com.liang.server;

import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description: 处理那些客户端需要我们监听的端口，这里只监听新的连接请求
 * @Author: LiangYang
 * @Date: 2022/4/27 下午6:43
 **/
@Slf4j
public class ServerListenNewConnectHandler implements Runnable{
    private ServerSocket listenSocket;
    private Socket clientSocket;
    int port;
    public ServerListenNewConnectHandler(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
        port = listenSocket.getLocalPort();
        clientSocket = ServerMapUtil.serverPortMap.get(port);
    }

    @Override
    public void run() {
        while  (clientSocket.isConnected() && !clientSocket.isClosed()){
            try {
                Socket socketWan = listenSocket.accept();
                log.info("Server\t端口{}接收到连接 socketWan:{}", port, socketWan.getInetAddress());
                assert socketWan.isConnected();
                String socketWanStr = socketWan.toString();
                ServerMapUtil.socketWanMap.put(socketWanStr, socketWan);
                Socket clientSocket = ServerMapUtil.serverPortMap.get(port);
                if (clientSocket.isConnected() && !clientSocket.isClosed()){
                    OutputStream out = clientSocket.getOutputStream();
                    byte[] bytes = socketWanStr.getBytes();
                    synchronized (out){
                        out.write(ByteUtil.intToByte(MessageFlag.eventNewConnect));
                        out.write(ByteUtil.intToByteArray(port));
                        out.write(ByteUtil.intToByte(bytes.length));
                        out.write(bytes);
                    }
//                    out.flush();
                } else {
                    log.info("Server\t客户端断开了连接->{}", clientSocket);
                    break;
                }
            } catch (IOException e) {
                log.info("Server\t{}端口无法接收新的连接申请异常, serverSocket:{}", port, listenSocket);
                e.printStackTrace();
                try {
                    listenSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            listenSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Server\t客户端断开了连接{}, 服务器端口[{}]停止监听", clientSocket, port);
    }
}
