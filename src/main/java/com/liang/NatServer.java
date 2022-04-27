package com.liang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 上午9:24
 **/
public class NatServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(25905);
        while (true){
            Socket wanSocket = serverSocket.accept();
            Socket lanSocket = new Socket("192.168.0.202",5905);
            new Thread(new NatServerWanHandler(wanSocket.getInputStream(), lanSocket.getOutputStream())).start();
            new Thread(new NatServerLanHandler(lanSocket.getInputStream(), wanSocket.getOutputStream())).start();
        }
    }
}
class NatServerWanHandler implements Runnable{
    InputStream wanInputStream;
    OutputStream lanOutputStream;

    public NatServerWanHandler(InputStream wanInputStream, OutputStream lanOutputStream) {
        this.wanInputStream = wanInputStream;
        this.lanOutputStream = lanOutputStream;
    }

    @Override
    public void run() {
        try {
            while(true) {
                byte[] bytes = new byte[1024];
                int read = wanInputStream.read(bytes);
                if (read>0){
                    lanOutputStream.write(bytes, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


class NatServerLanHandler implements Runnable{
    InputStream lanInputStream;
    OutputStream wanOutputStream;

    public NatServerLanHandler(InputStream lanInputStream, OutputStream wanOutputStream) {
        this.lanInputStream = lanInputStream;
        this.wanOutputStream = wanOutputStream;
    }

    @Override
    public void run() {
        try {
            while(true) {
                byte[] bytes = new byte[1024];
                int read = lanInputStream.read(bytes);
                if (read>0){
                    wanOutputStream.write(bytes, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

