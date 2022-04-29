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
    private ServerSocket listenSocket;
    private Socket clientSocket;
    int port;
    public ServerListenRemotePortAcceptHandler(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
        port = listenSocket.getLocalPort();
        clientSocket = ServerMapUtil.serverPortMap.get(port);
        log.info("Server\t开启线程监听云端端口 [{}],与客户端进行信息交互的Socket为{}", port, clientSocket);
    }

    @Override
    public void run() {
        while  (clientSocket.isConnected() && !clientSocket.isClosed()){
            try {
                Socket socketWan = listenSocket.accept();
                log.info("Server\t云端端口[{}]接收到连接,生成socketWan:{}", port, socketWan);
                assert socketWan.isConnected();
                String socketWanStr = socketWan.toString();
                ServerMapUtil.socketWanMap.put(socketWanStr, socketWan);
                Socket server2clientSocket = ServerMapUtil.serverPortMap.get(port);     // 获取想要监听该端口的客户端socket
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
                    new Thread(new Server2ClientForwarder(socketWan)).start();
                } else {
                    log.warn("Server\t客户端断开了连接->{}", server2clientSocket);
                    break;
                }
            } catch (IOException e) {
                log.warn("Server\t{}端口无法接收新的连接申请异常, serverSocket:{}", port, listenSocket);
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
