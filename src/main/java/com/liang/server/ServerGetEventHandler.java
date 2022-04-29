package com.liang.server;

import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @Description: 服务器处理事件
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:44
 **/

@Slf4j
class ServerGetEventHandler implements Runnable{
    Socket clientSocket;

    public ServerGetEventHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        log.info("Server\t开始事件监听");
    }

    public int loginHandler(InputStream in) throws IOException {
        int read;
        byte[] countBytes = new byte[4];
        read = in.read(countBytes);
        if (read == -1) {
            log.warn("Server\t客户端登录事件异常");
            return -1;
        }
        int count = ByteUtil.byteArrayToInt(countBytes);
        byte[] portsBytes = new byte[count * 4];
        read = in.read(portsBytes);
        if (read != count * 4) {
            log.warn("Server\t客户端登录事件异常,数据长度不足");
            return -1;
        }
        int listenCount = 0;
        for (int i = 0; i < count; i++) {
            int port = ByteUtil.byteArrayToInt(portsBytes, i * 4);
            log.info("Server\t注册并监听映射端口: " + port);
            boolean listenSuccessFlag = false;
            ServerSocket serverSocket=null;
            try {
                serverSocket = new ServerSocket(port);
                listenSuccessFlag = true;
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Server\t监听端口失败: " + port);
            }
            if (listenSuccessFlag) {
                ServerMapUtil.serverPortMap.put(port, clientSocket);    //记录该端口是这个客户端要监听的
                log.info("Server\t监听端口成功: {}" ,port);
                new Thread(new ServerListenRemotePortAcceptHandler(serverSocket)).start();
                listenCount++;
            }
        }
        return listenCount;
    }

    public void forward(InputStream in) throws IOException {
        log.trace("Server\t处理转发事件");
        int read;
        read = in.read();
        if (read == -1) {
            log.warn("Server\t转发事件异常");
            return;
        }
        int nameCount = read;
        byte[] nameBytes = new byte[nameCount];
        read = in.read(nameBytes);
        if (read != nameCount) {
            log.warn("Server\t转发事件异常");
            return;
        }
        byte[] countBytes = new byte[4];
        read = in.read(countBytes);
        if (read != 4) {
            log.warn("Server\t转发事件异常，消息体长度未指定");
            return;
        }
        int count = ByteUtil.byteArrayToInt(countBytes);
        byte[] bytes = new byte[count];
        read = in.read(bytes);
        if (read != count) {
            log.warn("Server\t转发事件异常,消息长度不一致,消息头指定长度{}，实际读取长度{}",count, read);
            return;
        }
        Socket socketWan = ServerMapUtil.socketWanMap.get(new String(nameBytes));
        OutputStream out = socketWan.getOutputStream();
        out.write(bytes, 0, read);
        out.flush();
        log.trace("Server\t本次转发事件完成，消息体长度{},转发携带{},转发至{}", read, new String(nameBytes),socketWan);
    }


    @Override
    public void run() {
        while (clientSocket.isConnected() && !clientSocket.isClosed()){
            try {
                InputStream in = clientSocket.getInputStream();
                // TODO 多线程的支持
                int eventIndex;
                eventIndex = in.read();
                if (eventIndex==-1) {
                    log.info("Server\tsocket对应的输入流关闭: {} ",clientSocket);
                    clientSocket.close();
                }
                log.trace("Server\t事件发生: " + MessageFlag.getComment(eventIndex));
                if (eventIndex == MessageFlag.eventLogin) {
                    log.info("Server\t客户端登录事件发生");
                    int listenCount = loginHandler(in);
                    if (listenCount<=0){
                        log.info("Server\t无监听事件，关闭当前客户端 {}", clientSocket);
                        clientSocket.close();
                    }
                    log.info("Server\t共监听{}个端口",listenCount);
                } else if (eventIndex == MessageFlag.eventForward){
                    forward(in);
                } else {
                    log.warn("Server\t非正常事件标志 [{}]", eventIndex);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Server\t客户端退出 {}",clientSocket);
    }
}

