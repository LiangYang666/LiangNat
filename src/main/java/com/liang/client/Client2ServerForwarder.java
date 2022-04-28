package com.liang.client;

import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: 客户端监听本地端口 有消息时向服务端转发
 * @Author: LiangYang
 * @Date: 2022/4/28 上午8:40
 **/
@Slf4j
public class Client2ServerForwarder implements Runnable{
    Socket socketLan;
    public Client2ServerForwarder(Socket socketLan) {
        log.info("Client\t{}开启客户端向服务端的端口消息转发", socketLan);
        this.socketLan = socketLan;
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        while (socketLan.isConnected() && !socketLan.isClosed()){
            try {
                InputStream in = socketLan.getInputStream();
                int read = in.read(bytes);
                if (read == -1){
                    log.info("Client\t转发消息时，输入流结束，端口号为{},socket为{}", socketLan.getLocalPort(),socketLan);
                    continue;
//                    socketLan.close();
                }

                OutputStream out = NatClient.client2serverSocket.getOutputStream(); // TODO 这里是否需要查看client2serverSocket是否已经关闭
                byte[] remoteSocketWanNames = ClientMapUtil.socketLanMap.get(socketLan);
                synchronized (out){
                    out.write(ByteUtil.intToByte(MessageFlag.eventForward));    // TODO 查看是否可以直接发
                    out.write(ByteUtil.intToByte(remoteSocketWanNames.length));
                    out.write(remoteSocketWanNames);
                    out.write(ByteUtil.intToByteArray(read));
                    out.write(bytes, 0, read);
                    out.flush();
                }
                log.trace("Client\t本地端口转发消息成功，消息体长度{}，转发携带{}，转发至{}",read, new String(remoteSocketWanNames), NatClient.client2serverSocket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Client\t本次穿透连接关闭，socket->{}", socketLan);
    }
}
