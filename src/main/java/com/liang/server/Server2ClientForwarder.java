package com.liang.server;

import com.liang.client.ClientMapUtil;
import com.liang.client.NatClient;
import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: 监听已经建立了连接的socket 得到数据时则进行转发
 * @Author: LiangYang
 * @Date: 2022/4/28 上午8:54
 **/
@Slf4j
public class Server2ClientForwarder implements Runnable{
    Socket socketWan;
    public Server2ClientForwarder(Socket socketWan) {
        this.socketWan = socketWan;
        log.info("Server\t{}开启服务端向客户端的端口消息转发", socketWan);
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        byte[] socketWanNames = socketWan.toString().getBytes();
        Socket server2clientSocket = ServerMapUtil.serverPortMap.get(socketWan.getLocalPort());
        while (socketWan.isConnected() && !socketWan.isClosed()){
            try {
                InputStream in = socketWan.getInputStream();
                int read = in.read(bytes);
                if (read == -1){
                    log.info("Server\t转发消息时，输入流结束，端口号为{}", socketWan.getLocalPort());
                    socketWan.close();
                    break;
                }
                OutputStream out = server2clientSocket.getOutputStream(); // TODO 这里是否需要查看client2serverSocket是否已经关闭
                log.trace("Server\t转发携带->{}",socketWan);
                synchronized (out){
                    out.write(ByteUtil.intToByte(MessageFlag.eventForward));    // TODO 查看是否可以直接发
                    out.write(ByteUtil.intToByte(socketWanNames.length));
                    out.write(socketWanNames);
                    out.write(ByteUtil.intToByteArray(read));
                    out.write(bytes, 0, read);
                    out.flush();
                }
                log.trace("Server\t云端端口转发消息成功，消息体长度{}，转发携带{}，转发至{}",read, socketWan, server2clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Server\t本次穿透连接关闭，socket->{}", socketWan);
    }
}
