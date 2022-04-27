package com.liang.client;


/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午4:58
 **/

public class ClientPortMapConfig {
    public String name;
    public String localIp;
    public int localPort;
    public int remotePort;

    public ClientPortMapConfig(String name, String localIp, int localPort, int remotePort) {
        this.name = name;
        this.localIp = localIp;
        this.localPort = localPort;
        this.remotePort = remotePort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public String toString() {
        return "ClientPortMap{" +
                "name='" + name + '\'' +
                ", localIp='" + localIp + '\'' +
                ", localPort=" + localPort +
                ", remotePort=" + remotePort +
                '}';
    }
}
