package com.liang.server;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 使用到的map字典，映射端口到socket和socket名到socket的关系，便于转发消息时知道是谁连接的
 * @Author: LiangYang
 * @Date: 2022/4/27 下午7:49
 **/
public class ServerMapUtil {

    static ConcurrentHashMap<String, Socket> socketWanMap = new ConcurrentHashMap<>();  // 服务端监听的穿透端口(客户端想监听的，例如25901)，
    // 这样的端口每来一个新连接请求，将socket的名称，map对应的socket， 每次转发消息时携带这个socket的名称字符串，就知道是这个socket发的消息

}
