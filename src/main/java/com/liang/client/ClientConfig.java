package com.liang.client;

import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午5:05
 **/
public class ClientConfig {
    public String serverAddress;
    public int serverPort;
    private final String token;
    public List<ClientPortMapConfig> portMap;

    public ClientConfig(String serverAddress, int serverPort, String token, List<ClientPortMapConfig> portMap) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.token = token;
        this.portMap = portMap;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getToken() {
        return token;
    }

    public List<ClientPortMapConfig> getPortMap() {
        return portMap;
    }

    public void setPortMap(List<ClientPortMapConfig> portMap) {
        this.portMap = portMap;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "serverAddress='" + serverAddress + '\'' +
                ", serverPort=" + serverPort +
                ", portMap=" + portMap +
                '}';
    }
}
