package com.liang.proxy;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午10:57
 **/
@Slf4j
public class Socks5ProxyListener implements Runnable{

    ServerSocket proxyLocalServerSocket;
    Socket client2serverSocket;

    public Socks5ProxyListener(ServerSocket proxyLocalServerSocket, Socket client2serverSocket) {
        this.proxyLocalServerSocket = proxyLocalServerSocket;
        this.client2serverSocket = client2serverSocket;
    }

    @Override
    public void run() {
        try {
            while (client2serverSocket.isConnected() && !client2serverSocket.isClosed()) {
                Socket socketProxyTransfer = proxyLocalServerSocket.accept();
                log.trace("Socks5\t本地代理监听端口接收到连接请求{}", socketProxyTransfer);
                new Thread(new Socks5ProxyHandler(socketProxyTransfer), "Socks5ProxyHandler-"+socketProxyTransfer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            proxyLocalServerSocket.close();
            log.warn("Socks5\t关闭代理监听并释放资源");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
