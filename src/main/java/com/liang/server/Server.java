package com.liang.server;



import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.liang.web.WebApp;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public static void main(String[] args) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("root");
        String configFilePath = "config_server.yaml";
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
        FileInputStream in = null;
        try {
            in = new FileInputStream(configFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(in, Map.class);
        Map common = (Map) map.get("common");
        Integer bindPort = (Integer) common.get("bind_port");
        Object tokenRaw = common.get("token");
        String token = tokenRaw == null ? "123456" : tokenRaw.toString();

        new Thread(new ServerStart(bindPort, token), "ServerMain").start();   // 以线程方式启动
        if (map.get("web")!=null){  // 如果有该参数，那么意味着使用web进行管理ip 不在ip白名单内的将不允许连接
            Map web = (Map) map.get("web");
            Integer web_port = (Integer) web.get("bind_port");
            String username = web.get("username") == null? "admin":web.get("username").toString();
            String password = web.get("password") == null? "123456":web.get("password").toString();
            WebApp.start(web_port, username, password, args);
        }

    }
    static class ServerStart implements Runnable{
        ServerSocket serverSocket;
        String token;
        public ServerStart(int bindPort, String token) {
            this.token = token;
            try {
                serverSocket = new ServerSocket(bindPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("Server\t服务启动，开始监听端口 "+ bindPort);
        }

        @Override
        public void run() {
            while (true){
                Socket server2clientSocket = null;
                try {
                    server2clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info("Server\t服务端口接收到连接请求 来自{}", server2clientSocket.getRemoteSocketAddress());
                new Thread(new ServerGetEventHandler(server2clientSocket, token), "ServerGetEvent"+server2clientSocket.getRemoteSocketAddress()).start();   // 开启事件监听
            }
        }
    }
}
