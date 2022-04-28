package com.liang.client;

import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: 客户端接收到事件的处理
 * @Author: LiangYang
 * @Date: 2022/4/27 下午10:19
 **/
@Slf4j
public class ClientGetEventHandler implements Runnable{
    Socket client2serverSocket;

    public ClientGetEventHandler(Socket socket) {
        this.client2serverSocket = socket;
    }
    public void newConnectHandler(InputStream in) throws IOException {
        int read;
        byte[] portBytes = new byte[4];
        read = in.read(portBytes);
        if (read == -1) {
            log.info("Server\t新连接事件异常");
            return;
        }
        int count = in.read();
        if (count == -1) {
            log.info("Server\t新连接事件异常，count=-1");
            return;
        }
        byte[] remoteSocketStrBytes = new byte[count];
        read = in.read(remoteSocketStrBytes);
        if (count!=read){
            log.info("Server\t新连接事件异常，远程socket信息数据长度不足");
            return;
        }
        int remotePort = ByteUtil.byteArrayToInt(portBytes);
        int localPort = ClientMapUtil.remotePortMap.get(remotePort).getLocalPort();
        String localIp = ClientMapUtil.remotePortMap.get(remotePort).getLocalIp();
        Socket socket = new Socket(localIp, localPort);
        // TODO 判断是否连接成功
        ClientMapUtil.socketLanMap.put(socket, remoteSocketStrBytes);
        ClientMapUtil.socketStringLanMap.put(new String(remoteSocketStrBytes), socket);
    }
    public void forward(InputStream in) throws IOException {
        int read;
        read = in.read();
        if (read == -1) {
            log.warn("Client\t转发事件异常");
            return;
        }
        int nameCount = read;
        byte[] nameBytes = new byte[nameCount];
        read = in.read(nameBytes);
        if (read != nameCount) {
            log.warn("Client\t转发事件异常");
            return;
        }
        Socket socketLan = ClientMapUtil.socketStringLanMap.get(new String(nameBytes));
        OutputStream out = socketLan.getOutputStream();
        byte[] bytes = new byte[1024];
        read = in.read(bytes);
        if (read == -1) {
            log.warn("Client\t转发事件异常");
            return;
        }
        out.write(bytes, 0, read);
    }
    @Override
    public void run() {
        while (client2serverSocket.isConnected() && !client2serverSocket.isClosed()) {
            try {
                InputStream in = client2serverSocket.getInputStream();
                int eventIndex;
                eventIndex = in.read();
                if (eventIndex==-1) {
                    log.warn("Client\tsocket对应的输入流关闭: {}，将关闭socket ",client2serverSocket);
                    client2serverSocket.close();
                }
                log.info("Client\t事件索引： " + eventIndex);
                if (eventIndex == MessageFlag.eventNewConnect){
                    newConnectHandler(in);
                } else if (eventIndex == MessageFlag.eventForward){
                    forward(in);
                } else {
                    log.warn("Client\t无效事件索引： " + eventIndex);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Client\t服务器连接中断，客户端退出");
    }
}
