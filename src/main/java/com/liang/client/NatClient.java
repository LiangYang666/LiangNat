package com.liang.client;

import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import com.liang.server.NatServer;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:47
 **/
@Slf4j
public class NatClient {
    static Socket client2serverSocket;

    public static ClientConfig clientInitConfig() throws FileNotFoundException {
        FileInputStream in = new FileInputStream("config_client.yaml");
        Yaml yaml = new Yaml();
        Map config = yaml.loadAs(in, Map.class);
        System.out.println(config);
        Map common = (Map) config.get("common");
        String serverAddress = (String) common.get("server_addr");
        Integer serverPort = (Integer) common.get("server_port");
        String token = common.get("token").toString();
        Map<String, Map<String, Object>> natMap = (Map<String, Map<String, Object>>) config.get("nat");
        ArrayList<ClientPortMapConfig> portWantMap = new ArrayList<>();
        for (String name : natMap.keySet()) {
            Map<String, Object> natEntry = natMap.get(name);
            //
            String localIp = (String) natEntry.get("local_ip");
            Integer localPort = (Integer) natEntry.get("local_port");
            Integer remotePort = (Integer) natEntry.get("remote_port");
            portWantMap.add(new ClientPortMapConfig(name, localIp, localPort, remotePort));
        }
        ClientConfig clientConfig = new ClientConfig(serverAddress, serverPort, token, portWantMap);
        System.out.println(portWantMap);
        return clientConfig;
    }

    public static void main(String[] args) throws FileNotFoundException {
        ClientConfig clientConfig = clientInitConfig();
        List<ClientPortMapConfig> portWantMap = clientConfig.getPortMap();
        for (int i = 0; i < portWantMap.size(); i++) {
            ClientMapUtil.remotePortMap.put(portWantMap.get(i).getRemotePort(), portWantMap.get(i));
        }
        byte[] tokenBytes = clientConfig.getToken().getBytes();
        while(true){
            try {
                client2serverSocket = new Socket(clientConfig.serverAddress, clientConfig.serverPort);
                log.info("Client\t成功连接到远程服务器 socket-> "+client2serverSocket);
                log.info("Client\t服务端:"+clientConfig.serverAddress+" 端口:"+clientConfig.serverPort);
                log.info("Client\t客户端:"+client2serverSocket.getLocalAddress()+" 端口:"+client2serverSocket.getLocalPort());
                OutputStream out = client2serverSocket.getOutputStream();
                out.write(MessageFlag.eventLogin);   // 写事件
                out.write(tokenBytes.length);   // 写密码长度
                out.write(tokenBytes);      // 写密码
                out.write(ByteUtil.intToByteArray(clientConfig.portMap.size()));    // 写端口数量
                for (int i = 0; i < clientConfig.portMap.size(); i++) {     // 写云端口
                    out.write(ByteUtil.intToByteArray(clientConfig.portMap.get(i).remotePort));
                }
                Thread workThread = new Thread(new ClientGetEventHandler(client2serverSocket));
                workThread.start(); // 开启事件监听
                workThread.join();
            }catch (ConnectException e) {
                log.warn("目标"+e.getMessage());
                e.printStackTrace();
            }catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            int sleepSecond = 5;
            log.info("Client\t{}秒后尝试重新登录到服务器", sleepSecond);
            try {
                Thread.sleep(sleepSecond*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
