package com.liang.server;

import com.liang.common.AESUtil;
import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @Description: 监听已经建立了连接的socket 得到数据时则进行转发
 * @Author: LiangYang
 * @Date: 2022/4/28 上午8:54
 **/
@Slf4j
public class Server2ClientForwarder implements Runnable{
    private final Socket socketWan;
    private final Socket server2clientSocket;
    public Server2ClientForwarder(Socket socketWan, Socket server2clientSocket) {
        this.socketWan = socketWan;
        this.server2clientSocket = server2clientSocket;
        log.info("Server\t{}开启服务端向客户端的端口消息转发", socketWan);
    }

    @Override
    public void run() {
        AESUtil aesUtil = new AESUtil();
        byte[] data = new byte[1024];
        byte[] socketWanNameBytes = socketWan.toString().getBytes();
        byte[] socketWanNameEncryptBytes = aesUtil.encrypt(socketWanNameBytes);
        while (socketWan.isConnected() && !socketWan.isClosed()){
            try {
                InputStream in = socketWan.getInputStream();
                int read = in.read(data);
                OutputStream out = server2clientSocket.getOutputStream(); // TODO 这里是否需要查看client2serverSocket是否已经关闭
                if (read == -1){
                    log.info("Server\t转发消息时，云端口[{}]输入流结束", socketWan.getLocalPort());
                    synchronized (out){
                        out.write(MessageFlag.eventCloseConnect);
                        out.write(socketWanNameEncryptBytes.length);
                        out.write(socketWanNameEncryptBytes);
//                        out.flush();
                    }
                    log.info("Server\t转发关闭连接标志，携带[{}]", socketWan);
                    break;
                }
                byte[] encryptedData = aesUtil.encrypt(data, 0, read);
                log.trace("Server\t转发携带->{}",socketWan);
                synchronized (out){
                    out.write(MessageFlag.eventForward);
                    out.write(socketWanNameEncryptBytes.length);
                    out.write(socketWanNameEncryptBytes);
                    out.write(ByteUtil.intToByteArray(encryptedData.length));
                    out.write(encryptedData);
//                    out.flush();
                }
                log.trace("Server\t云端端口转发消息成功，消息体长度{}，转发携带{}，转发至{}",read, socketWan, server2clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Server\t转发消息异常，关闭连接");
                break;
            }
        }
        try {
            socketWan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerMapUtil.socketWanMap.remove(socketWan.toString());
        log.info("Server\t本次穿透连接关闭，线程退出，socket->{}", socketWan);
    }
}
