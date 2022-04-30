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
public class ServerListenRemotePortAcceptHandler implements Runnable{
    private final ServerSocket listenSocket;
    private final Socket server2clientSocket;
    private final int port;
    public ServerListenRemotePortAcceptHandler(ServerSocket listenSocket, Socket server2clientSocket) {
        this.listenSocket = listenSocket;
        port = listenSocket.getLocalPort();
        this.server2clientSocket = server2clientSocket;
        log.info("Server\t开启线程监听云端端口 [{}],与客户端进行信息交互的Socket为{}", port, server2clientSocket);
    }

    @Override
    public void run() {
        while  (server2clientSocket.isConnected()
                && !server2clientSocket.isClosed()
                && !listenSocket.isClosed()){
            try {
                Socket socketWan = listenSocket.accept();
                log.info("Server\t云端端口[{}]接收到连接,生成socketWan:{}", port, socketWan);
                assert socketWan.isConnected();
                String socketWanStr = socketWan.toString();
                ServerMapUtil.socketWanMap.put(socketWanStr, socketWan);
                if (server2clientSocket.isConnected() && !server2clientSocket.isClosed()){
                    OutputStream out = server2clientSocket.getOutputStream();
                    byte[] bytes = socketWanStr.getBytes();
                    synchronized (out){
                        out.write(ByteUtil.intToByte(MessageFlag.eventNewConnect));
                        out.write(ByteUtil.intToByteArray(port));
                        out.write(ByteUtil.intToByte(bytes.length));
                        out.write(bytes);
//                        out.flush();
                    }
                    log.trace("Server\t新连接请求发送完成");
                    new Thread(new Server2ClientForwarder(socketWan, server2clientSocket)).start();
                } else {
                    log.warn("Server\t客户端断开了连接->{}", server2clientSocket);
                    break;
                }
            } catch (IOException e) {
                log.warn("Server\t无法监听端口{}, {}", port, e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        if (!listenSocket.isClosed()){
            try {
                listenSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Server\t云端端口[{}]停止监听并释放线程", port);
    }
}
