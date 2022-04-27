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

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 10101;
        ArrayList<ClientPortMapConfig> portWantMap = new ArrayList<>();
        portWantMap.add(new ClientPortMapConfig("vnc5905", "192.168.0.202", 45905, 5905));
        portWantMap.add(new ClientPortMapConfig("vnc5901", "192.168.0.202", 45901, 5901));
        ClientConfig clientConfig = new ClientConfig(serverAddress, serverPort, portWantMap);
        try {
            Socket clientSocket = new Socket(serverAddress, serverPort);
            log.info("Client\t连接远程服务器:"+serverAddress+":"+serverPort);
            OutputStream out = clientSocket.getOutputStream();
            out.write(ByteUtil.intToByte(1));
            out.write(ByteUtil.intToByteArray(clientConfig.portMap.size()));
            for (int i = 0; i < clientConfig.portMap.size(); i++) {
                out.write(ByteUtil.intToByteArray(clientConfig.portMap.get(i).remotePort));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
