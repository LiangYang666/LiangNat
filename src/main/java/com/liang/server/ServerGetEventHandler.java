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

    public ServerGetEventHandler(Socket server2clientSocket, String token) {
        this.server2clientSocket = server2clientSocket;
        this.token = token;
        listenSocketList = new ArrayList<>();
        log.info("Server\t开始事件监听");
    }

    public int loginHandler(InputStream in) throws IOException {
        int read;
        int tokenBytesCount = in.read();
        if (tokenBytesCount <= 0) {
            log.warn("Server\t客户端登录事件异常，密码长度未指定或非法");
            return -1;
        }
        tokenBytesCount = AESUtil.countPadding(tokenBytesCount);   // 是加密字段需要补全长度
        byte[] tokenBytes = new byte[tokenBytesCount];
        read = in.read(tokenBytes);
        if (read != tokenBytesCount) {
            log.warn("Server\t客户端登录事件异常，密码获取失败或非法");
            return -1;
        }
        tokenBytes = AESUtil.decrypt(tokenBytes);   // 解密
        if (! new String(tokenBytes).equals(token)){
            log.warn("Server\t客户端登录事件异常，密码错误");
            return -1;
        }
        loginSuccess = true;
        byte[] countBytes = new byte[4];
        read = in.read(countBytes);
        if (read == -1) {
            log.warn("Server\t客户端登录事件异常，端口数不合法");
            return -1;
        }
        int count = ByteUtil.byteArrayToInt(countBytes);
        int bytesCount = count*4;
        bytesCount = AESUtil.countPadding(bytesCount);
        byte[] portsBytes = new byte[bytesCount];    // 每个端口号字节码将加密 4个字节将会补全为16个字节
        read = in.read(portsBytes);
        if (read != bytesCount) {
            log.warn("Server\t客户端登录事件异常，端口数据长度不足");
            return -1;
        }
        portsBytes = AESUtil.decrypt(portsBytes);
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
        int nameCount = in.read();
        if (nameCount == -1) {
            log.warn("Server\t转发事件异常，socket名称长度未指定");
            return;
        }
        byte[] nameBytes = new byte[nameCount];
        int read = in.read(nameBytes);
        if (read != nameCount) {
            log.warn("Server\t转发事件异常，socket名称获取失败");
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
        if (socketWan==null || socketWan.isClosed()){
            log.warn("Server\t转发事件异常"+new String(nameBytes)+"对应的socketWan"+(socketWan==null?"为null":socketWan+"已关闭"));
            return;
        }
        OutputStream out = socketWan.getOutputStream();
        out.write(bytes, 0, read);
        out.flush();
        log.trace("Server\t本次转发事件完成，消息体长度{},转发携带{},转发至{}", read, new String(nameBytes),socketWan);
    }

    public void closeConnect(InputStream in) throws IOException {
        log.trace("Server\t处理关闭连接事件");
        int nameCount = in.read();
        if (nameCount == -1) {
            log.warn("Server\t关闭连接事件异常，socket名称长度未指定");
            return;
        }
        byte[] nameBytes = new byte[nameCount];
        int read = in.read(nameBytes);
        if (read != nameCount) {
            log.warn("Server\t关闭连接事件异常，socket名称获取失败");
            return;
        }
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

