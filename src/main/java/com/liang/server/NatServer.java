package com.liang.server;



import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 上午9:24
 **/
@Slf4j
public class NatServer {
    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("config_server.yaml");
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(in, Map.class);
        Map common = (Map) map.get("common");
        Integer bindPort = (Integer) common.get("bind_port");
        Object tokenRaw = common.get("token");
        String token = tokenRaw == null ? "123456 " : tokenRaw.toString();

        ServerSocket serverSocket = new ServerSocket(bindPort);
        log.info("Server\t服务启动，开始监听端口 "+ bindPort);

        while (true){
            Socket wanSocket = serverSocket.accept();
            log.info("Server\t接收到登录请求: "+ wanSocket);
            new Thread(new ServerGetEventHandler(wanSocket)).start();   // 开启事件监听
        }
    }
}
