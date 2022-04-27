package com.liang.common;

import org.junit.Test;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午2:55
 **/
public class MessageFlag {
    // 消息体定义规范 第一个字节为标志位 标志位有以下几种
    public static final Integer eventLogin = 1;
    // 标志为1时
    // 第2、3、4、5四个个字节表示需要监听端口的总数portCount
    // 之后的字节共有4*portCount 每个字节表示一个想要监听的端口

    public static final Integer eventAccept = 2;
    // 标志为2时
    // 第2、3、4、5四个字节表示发生接收连接的端口号port
    // 第6个字节表示后面socket名称字符串的byte数组长度count
    // 之后的count个字节表示socket名称的字节数组

    public static final Integer eventForward = 3;
}
