package com.liang.common;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午2:55
 **/
public class MessageFlag {
    // 消息体定义规范 第一个字节为标志位 标志位有以下几种
    public static final int eventLogin = 1;
    // 是客户端登录事件
    // 第2个字节为密码数组长度(加密后的)
    // 第3个到后面为密码字节 (加密后的)
    // 密码后的四个个字节表示需要监听端口的信息字节数组的长度portBytesLength (加密后的)
    // 之后的字节共有portBytesLength个(加密后的)， 解密后每4个字节表示一个想要监听的端口

    public static final int eventNewConnect = 2;
    // 表示产生新连接事件，例如有用户访问云端的20022号映射端口了，那么就需要处理
    // 第2-17，一共16个字节表示发生接收连接的端口号port (因为加密，所以16字节)
    // 第18个字节表示后面socket名称字符串的byte数组长度nameLength(加密后的长度)
    // 之后的nameLength个字节表示socket名称的字节数组 (加密后的)

    public static final int eventCloseConnect = 3;
    // 表示新关闭连接的事件，例如用户断开了云端20022号映射端口了，或者内网22号(被云20022映射的)端口的进程主动断开了
    // 第2个字节表示后面socket名称字符串的byte数组长度nameLength (加密后的长度)
    // 之后的nameLength 个字节表示socket名称的字节数组 (加密后的)

    public static final int eventForward = 4;
    // 是转发事件
    // 第2个字节表示 云端socket名称byte数组的长度nameLength
    // 后面nameLength个字节表示云端socket名称
    // 再后面4个字节表示消息内容的长度dataLength，最长为1024长度(由于加密，当恰好为1024时将会+16)
    // 之后的dataLength个字节表示加密后的消息

    static public String getComment(int flag){
        switch(flag) {
            case eventLogin: return "eventLogin ["+flag+"]";
            case eventNewConnect: return "eventNewConnect ["+flag+"]";
            case eventCloseConnect: return "eventCloseConnect ["+flag+"]";
            case eventForward: return "eventForward ["+flag+"]";
            default: return "invalid flag ["+flag+"]";
        }
    }
}
