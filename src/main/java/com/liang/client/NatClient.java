package com.liang.client;

import com.liang.common.ByteUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:47
 **/
@Slf4j
public class NatClient {
    static Socket client2serverSocket;

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 10101;
        ArrayList<ClientPortMapConfig> portWantMap = new ArrayList<>();
        portWantMap.add(new ClientPortMapConfig("vnc5905", "192.168.0.202", 5905, 45905));
        portWantMap.add(new ClientPortMapConfig("vnc5901", "192.168.0.202", 5901, 45901));
        portWantMap.add(new ClientPortMapConfig("ssh22", "192.168.0.202", 22, 40022));
        ClientConfig clientConfig = new ClientConfig(serverAddress, serverPort, portWantMap);
        for (int i = 0; i < portWantMap.size(); i++) {
            ClientMapUtil.remotePortMap.put(portWantMap.get(i).getRemotePort(), portWantMap.get(i));
        }
        try {
            client2serverSocket = new Socket(serverAddress, serverPort);
            log.info("Client\t连接远程服务器 socket-> "+client2serverSocket);
            log.info("Client\t服务端:"+serverAddress+" 端口:"+serverPort);
            log.info("Client\t客户端:"+client2serverSocket.getLocalAddress()+" 端口:"+client2serverSocket.getLocalPort());
            OutputStream out = client2serverSocket.getOutputStream();
            out.write(ByteUtil.intToByte(1));
            out.write(ByteUtil.intToByteArray(clientConfig.portMap.size()));
            for (int i = 0; i < clientConfig.portMap.size(); i++) {
                out.write(ByteUtil.intToByteArray(clientConfig.portMap.get(i).remotePort));
            }
            out.flush();
            new Thread(new ClientGetEventHandler(client2serverSocket)).start(); // 开启事件监听

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
