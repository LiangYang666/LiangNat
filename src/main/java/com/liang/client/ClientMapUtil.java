package com.liang.client;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 均是常量，存储map关系，记录一些socket的映射关系，方便转发时找到对应的socket
 * @Author: LiangYang
 * @Date: 2022/4/27 下午10:24
 **/
public class ClientMapUtil {
    static ConcurrentHashMap<Socket, byte[]> socketLanMap = new ConcurrentHashMap<>(); // 键为本地socket，String为云端对应的socket
    static ConcurrentHashMap<Integer, ClientPortMapConfig> remotePortMap = new ConcurrentHashMap<>(); // 键为本地云端端口，值为客户端端口配置

}
