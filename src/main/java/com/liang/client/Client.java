package com.liang.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.liang.common.AESUtil;
import com.liang.common.ByteUtil;
import com.liang.common.MessageFlag;
import com.liang.proxy.Socks5ProxyListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 客户端运行主程序
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:47
 **/
@Slf4j
public class Client {
    static Socket client2serverSocket;
    static final String socks5Proxy = "socks5_proxy";

    public static ClientConfig clientInitConfig(String configFilePath) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(configFilePath);
        Yaml yaml = new Yaml();
        Map config = yaml.loadAs(in, Map.class);
        log.info("Client\t配置文件内容为{}", config);
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
        if (config.get(socks5Proxy)!=null){  // 如果需要代理上网
            Map socks5ProxyMap = (Map) config.get(socks5Proxy);
            Integer remotePort = (Integer) socks5ProxyMap.get("remote_port");
            portWantMap.add(new ClientPortMapConfig(socks5Proxy, "127.0.0.1", 0, remotePort));   //本地端口暂时设置为0，等待成功登录到云端后再设置端口
        }
        ClientConfig clientConfig = new ClientConfig(serverAddress, serverPort, token, portWantMap);
        System.out.println(portWantMap);
        return clientConfig;
    }

    public static void main(String[] args) throws FileNotFoundException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("root");
        String configFilePath = "config_client.yaml";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("trace")){
                logger.setLevel(Level.TRACE);
            } else if(args[i].equals("info")){
                logger.setLevel(Level.INFO);
            }
            if (args[i].endsWith(".yaml")){
                configFilePath = args[i];
            }
        }
        ClientConfig clientConfig = clientInitConfig(configFilePath);
        List<ClientPortMapConfig> portWantMap = clientConfig.getPortMap();
        for (int i = 0; i < portWantMap.size(); i++) {
            ClientMapUtil.remotePortMap.put(portWantMap.get(i).getRemotePort(), portWantMap.get(i));
        }
        byte[] tokenBytes = clientConfig.getToken().getBytes();
        AESUtil aesUtil = new AESUtil();
        while(true){
            try {
                client2serverSocket = new Socket(clientConfig.serverAddress, clientConfig.serverPort);
                log.info("Client\t成功连接到远程服务器 socket-> "+client2serverSocket);
                log.info("Client\t服务端:"+clientConfig.serverAddress+" 端口:"+clientConfig.serverPort);
                log.info("Client\t客户端:"+client2serverSocket.getLocalAddress()+" 端口:"+client2serverSocket.getLocalPort());
                OutputStream out = client2serverSocket.getOutputStream();
                out.write(MessageFlag.eventLogin);   // 写事件
                byte[] encryptedToken = aesUtil.encrypt(tokenBytes);
                out.write(encryptedToken.length);   // 写密码长度
                out.write(encryptedToken);      // 写密码(并加密)
                byte[] portsBytes = new byte[clientConfig.portMap.size()*4];
                for (int i = 0; i < clientConfig.portMap.size(); i++) {     // 写云端口
                    byte[] temp = ByteUtil.intToByteArray(clientConfig.portMap.get(i).remotePort);
                    System.arraycopy(temp, 0, portsBytes, i * 4, 4);
                }
                byte[] encryptedPortsBytes = aesUtil.encrypt(portsBytes);
                out.write(ByteUtil.intToByteArray(encryptedPortsBytes.length));    // 写端口数组长度
                out.write(encryptedPortsBytes);     // 写端口
                // TODO: 判断云端是否接收成功，各端口是否成功监听
                ServerSocket socks5ProxyServerSocket = null;
                for (ClientPortMapConfig clientPortMapConfig : clientConfig.portMap) {
                    if (clientPortMapConfig.getName().equals(socks5Proxy)){  // 查看是否需要socks5_proxy代理
                        Socks5ProxyListener proxyListener = new Socks5ProxyListener();
                        socks5ProxyServerSocket  = proxyListener.getListenSocket();
                        if (socks5ProxyServerSocket != null){
                            clientPortMapConfig.setLocalPort(socks5ProxyServerSocket.getLocalPort());   // 设置映射端口
                            new Thread(proxyListener,
                                    "Socks5ProxyListener-bind-port-"+socks5ProxyServerSocket.getLocalPort()).start();  // 开启本地代理监听
                        }
                        break;
                    }
                }
                Thread workThread = new Thread(new ClientGetEventHandler(client2serverSocket), "ClientGetEvent"+client2serverSocket.getRemoteSocketAddress());
                workThread.start(); // 开启事件监听
                workThread.join();  // 等待线程结束
                if (socks5ProxyServerSocket!=null){ // 关闭连接则需要将代理服务也关闭
                    socks5ProxyServerSocket.close();
                }
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
