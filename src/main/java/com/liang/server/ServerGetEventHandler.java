package com.liang.server;

import com.liang.common.AESUtil;
import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * @Description: 服务器处理事件
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:44
 **/

@Slf4j
class ServerGetEventHandler implements Runnable{
    private final Socket server2clientSocket;       // 该服务端处理线程使用的信息交互socket
    private final List<ServerSocket> listenSocketList;    // 该信息交互通道上所有云端端口的监听serverSocket
    private boolean loginSuccess = false; //
    private final String token;
    private final AESUtil aesUtil;

    public ServerGetEventHandler(Socket server2clientSocket, String token) {
        this.server2clientSocket = server2clientSocket;
        this.token = token;
        listenSocketList = new ArrayList<>();
        log.info("Server\t开始事件监听");
        aesUtil = new AESUtil();
    }

    public int loginHandler(InputStream in) throws IOException {
        int read;
        int tokenEncryptedBytesLength = in.read();
        if (tokenEncryptedBytesLength <= 0) {
            log.warn("Server\t客户端登录事件异常，密码长度未指定或非法");
            return -1;
        }
        byte[] tokenEncryptedBytes = new byte[tokenEncryptedBytesLength];
        read = in.read(tokenEncryptedBytes);
        if (read != tokenEncryptedBytesLength) {
            log.warn("Server\t客户端登录事件异常，密码获取失败或非法");
            return -1;
        }
        byte[] tokenBytes = aesUtil.decrypt(tokenEncryptedBytes);   // 解密
        if (! new String(tokenBytes).equals(token)){
            log.warn("Server\t客户端登录事件异常，密码错误");
            return -1;
        }
        loginSuccess = true;
        byte[] portBytesLengthBytes = new byte[4];
        read = in.read(portBytesLengthBytes);
        if (read != 4) {
            log.warn("Server\t客户端登录事件异常，端口数不合法");
            return -1;
        }

        int bytesLength = ByteUtil.byteArrayToInt(portBytesLengthBytes);
        byte[] portsEncryptedBytes = new byte[bytesLength];
        read = in.read(portsEncryptedBytes);     // 读取需要监听的所有端口信息
        if (read != bytesLength) {
            log.warn("Server\t客户端登录事件异常，端口数据长度不足，需要{}，仅读取到{}", bytesLength, read);
            return -1;
        }
        byte[] portsBytes = aesUtil.decrypt(portsEncryptedBytes);
        int listenCount = 0;
        for (int i = 0; i < portsBytes.length/4; i++) {     // 遍历获取每个端口 每个端口占4个字节
            int port = ByteUtil.byteArrayToInt(portsBytes, i * 4);
            log.info("Server\t注册并监听映射端口: " + port);
            boolean listenSuccessFlag = false;
            ServerSocket serverSocket=null;
            try {
                serverSocket = new ServerSocket(port);
                listenSuccessFlag = true;
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Server\t监听端口失败: " + port+ ", " +e.getMessage());
            }
            if (listenSuccessFlag) {
                log.info("Server\t监听端口成功: {}" ,port);
                Thread thread = new Thread(new ServerListenRemotePortAcceptHandler(serverSocket, server2clientSocket), "Thread-listen["+port+"]");
                thread.start();
                listenSocketList.add(serverSocket);
                listenCount++;
            }
        }
        return listenCount;
    }

    public void forward(InputStream in) throws IOException {
        log.trace("Server\t处理转发事件");
        int encryptedNameLength = in.read();
        if (encryptedNameLength == -1) {
            log.warn("Server\t转发事件异常，socket名称长度未指定");
            return;
        }
        byte[] nameEncryptedBytes = new byte[encryptedNameLength];
        int read = in.read(nameEncryptedBytes);
        if (read != encryptedNameLength) {
            log.warn("Server\t转发事件异常，socket名称获取失败");
            return;
        }
        byte[] nameBytes = aesUtil.decrypt(nameEncryptedBytes);
        byte[] dataBytesLengthBytes = new byte[4];
        read = in.read(dataBytesLengthBytes);
        if (read != 4) {
            log.warn("Server\t转发事件异常，消息体长度未指定");
            return;
        }
        int encryptedDataLength = ByteUtil.byteArrayToInt(dataBytesLengthBytes);
        byte[] encryptedData = new byte[encryptedDataLength];
        read = in.read(encryptedData);
        if (read != encryptedDataLength) {
            log.warn("Server\t转发事件异常,消息长度不一致,消息头指定长度{}，实际读取长度{}",encryptedDataLength, read);
            return;
        }
        Socket socketWan = ServerMapUtil.socketWanMap.get(new String(nameBytes));
        if (socketWan==null || socketWan.isClosed()){
            log.warn("Server\t转发事件异常"+new String(nameBytes)+"对应的socketWan"+(socketWan==null?"为null":socketWan+"已关闭"));
            return;
        }
        byte[] data = aesUtil.decrypt(encryptedData);
        OutputStream out = socketWan.getOutputStream();
        out.write(data);
        out.flush();
        log.trace("Server\t本次转发事件完成，解密后消息体长度{},转发携带{},转发至{}", data.length, new String(nameBytes),socketWan);
    }

    public void closeConnect(InputStream in) throws IOException {
        log.trace("Server\t处理关闭连接事件");
        int encryptedNameLength = in.read();
        if (encryptedNameLength == -1) {
            log.warn("Server\t关闭连接事件异常，socket名称长度未指定");
            return;
        }
        byte[] encryptedNameBytes = new byte[encryptedNameLength];
        int read = in.read(encryptedNameBytes);
        if (read != encryptedNameLength) {
            log.warn("Server\t关闭连接事件异常，socket名称获取失败");
            return;
        }
        byte[] nameBytes = aesUtil.decrypt(encryptedNameBytes);
        Socket socketWan = ServerMapUtil.socketWanMap.get(new String(nameBytes));
        ServerMapUtil.socketWanMap.remove(new String(nameBytes));
        socketWan.close();
        log.info("Server\t处理关闭连接事件完成，成功关闭并移除socketWan{}", socketWan);
    }

    @Override
    public void run() {
        while (server2clientSocket.isConnected() && !server2clientSocket.isClosed()){
            try {
                InputStream in = server2clientSocket.getInputStream();
                int eventIndex;
                eventIndex = in.read();
                if (eventIndex==-1) {
                    log.info("Server\tsocket对应的输入流关闭: {} ", server2clientSocket.getRemoteSocketAddress());
                    server2clientSocket.close();
                }
                log.trace("Server\t收到事件: " + MessageFlag.getComment(eventIndex));
                if (!loginSuccess){         // 未登录  需要先登录
                    if (eventIndex == MessageFlag.eventLogin) {
                        log.info("Server\t客户端登录事件发生");
                        int listenCount = loginHandler(in);
                        if (!loginSuccess) {
                            log.warn("Server\t客户端登录失败，关闭当前客户端 {}", server2clientSocket.getRemoteSocketAddress());
                            break;
                        }
                        if (listenCount<=0){
                            log.info("Server\t客户端无监听事件，关闭当前客户端 {}", server2clientSocket.getRemoteSocketAddress());
                            break;
                        }
                        log.info("Server\t共监听{}个端口",listenCount);
                    } else {
                        log.warn("Server\t客户端未登录，关闭当前客户端 {}", server2clientSocket.getRemoteSocketAddress());
                    }
                } else {
                    if (eventIndex == MessageFlag.eventCloseConnect){
                        closeConnect(in);
                    }  else if (eventIndex == MessageFlag.eventForward){
                        forward(in);
                    } else {
                        log.warn("Server\t非正常事件标志 [{}]", eventIndex);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            server2clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Server\t客户端退出 {}, 将关闭各监听socket", server2clientSocket);
        listenSocketList.forEach(t->{
            try {
                t.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

