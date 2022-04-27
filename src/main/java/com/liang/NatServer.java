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
            new Thread(new ReadAndForwardHandler(wanSocket.getInputStream(), lanSocket.getOutputStream())).start();
            new Thread(new ReadAndForwardHandler(lanSocket.getInputStream(), wanSocket.getOutputStream())).start();
        }
    }
}

class ReadAndForwardHandler implements Runnable{
    InputStream inputStream;
    OutputStream outputStream;

    public ReadAndForwardHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    @Override
    public void run() {
        try {
            byte[] bytes = new byte[1024];
            int read;
            while (true){
                if ((read = inputStream.read(bytes)) >0){
                    outputStream.write(bytes, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
