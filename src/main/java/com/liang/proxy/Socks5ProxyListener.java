package com.liang.proxy;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description: socks5服务监听线程，有连接时新建线程进行验证和处理
 * @Author: LiangYang
 * @Date: 2022/5/26 下午10:57
 **/
@Slf4j
public class Socks5ProxyListener implements Runnable{

    private ServerSocket listenSocket=null; // 代理监听端口，将系统代理该为这个就可

    public Socks5ProxyListener() throws IOException {
        this.listenSocket = new ServerSocket(0);
    }

    public ServerSocket getListenSocket() {
        return listenSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socketProxyTransfer = listenSocket.accept();
                log.trace("Socks5\t本地代理监听端口接收到连接请求{}", socketProxyTransfer);
                new Thread(new Socks5ProxyHandler(socketProxyTransfer), "Socks5ProxyHandler-"+socketProxyTransfer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            listenSocket.close();
            log.warn("Socks5\t关闭代理监听并释放资源");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
