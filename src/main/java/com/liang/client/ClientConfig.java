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
    public List<ClientPortMapConfig> portMap;

    public ClientConfig(String serverAddress, int serverPort, List<ClientPortMapConfig> portMap) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
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
