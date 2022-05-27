package com.liang.server;

import com.liang.common.AESUtil;
import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

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
    private final AESUtil aesUtil;

    public ServerListenRemotePortAcceptHandler(ServerSocket listenSocket, Socket server2clientSocket) {
        this.listenSocket = listenSocket;
        port = listenSocket.getLocalPort();
        this.server2clientSocket = server2clientSocket;
        log.info("Server\t开启线程监听云端端口 [{}],与客户端进行信息交互的Socket为{}", port, server2clientSocket);
        aesUtil = new AESUtil();
    }

    public boolean checkIpIfPermission(String ip) throws UnknownHostException {  //判断该IP是否符合
        if(!AllowedIpUtil.ipSets.contains("web_control")){  // 不需要控制
            return true;
        }
        if (AllowedIpUtil.ipSets.contains(ip)){
            return true;
        }
        for (String ipSet : AllowedIpUtil.ipSets) {
            if (ipSet.contains("/")){
                if (AllowedIpUtil.checkIpBelongCIDR(ipSet, ip)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        while  (server2clientSocket.isConnected()
                && !server2clientSocket.isClosed()
                && !listenSocket.isClosed()){
            try {
                Socket socketWan = listenSocket.accept();
                String inetAddress = socketWan.getInetAddress().toString().substring(1);
                if (!checkIpIfPermission(inetAddress)){
                    log.info("Server\t防火墙拦截该连接，IP为{}", inetAddress);
                    socketWan.close();
                    continue;
                }
                log.info("Server\t云端端口[{}]接收到连接,生成socketWan:{}", port, socketWan);
                assert socketWan.isConnected();
                String socketWanStr = socketWan.toString();
                ServerMapUtil.socketWanMap.put(socketWanStr, socketWan);
                if (server2clientSocket.isConnected() && !server2clientSocket.isClosed()){
                    OutputStream out = server2clientSocket.getOutputStream();
                    byte[] nameBytes = socketWanStr.getBytes();
                    byte[] encryptedNameBytes = aesUtil.encrypt(nameBytes);
                    byte[] portBytes = ByteUtil.intToByteArray(port);
                    byte[] encryptedPortBytes = aesUtil.encrypt(portBytes);
                    synchronized (out){
                        out.write(MessageFlag.eventNewConnect);
                        out.write(encryptedPortBytes);
                        out.write(ByteUtil.intToByte(encryptedNameBytes.length));
                        out.write(encryptedNameBytes);
//                        out.flush();
                    }
                    log.trace("Server\t新连接请求发送完成");
                    new Thread(new Server2ClientForwarder(socketWan, server2clientSocket), "s2cForward-"+socketWan).start();
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
