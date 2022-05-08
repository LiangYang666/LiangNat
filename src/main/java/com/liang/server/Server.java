package com.liang.server;



import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * @Description: 服务端
 * @Author: LiangYang
 * @Date: 2022/4/27 上午9:24
 **/
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("root");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("trace")){
                logger.setLevel(Level.TRACE);
            } else if(args[i].equals("info")){
                logger.setLevel(Level.INFO);
            }
        }

        FileInputStream in = new FileInputStream("config_server.yaml");
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(in, Map.class);
        Map common = (Map) map.get("common");
        Integer bindPort = (Integer) common.get("bind_port");
        Object tokenRaw = common.get("token");
        String token = tokenRaw == null ? "1234 " : tokenRaw.toString();

        ServerSocket serverSocket = new ServerSocket(bindPort);
        log.info("Server\t服务启动，开始监听端口 "+ bindPort);

        while (true){
            Socket server2clientSocket = serverSocket.accept();
            log.info("Server\t服务端口接收到连接请求 来自{}", server2clientSocket.getRemoteSocketAddress());
            new Thread(new ServerGetEventHandler(server2clientSocket, token), "ServerGetEvent"+server2clientSocket.getRemoteSocketAddress()).start();   // 开启事件监听
        }
    }
}
