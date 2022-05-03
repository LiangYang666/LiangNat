package com.liang.client;

import com.liang.common.AESUtil;
import com.liang.common.ByteUtil;
import com.liang.common.IOReadUtil;
import com.liang.common.MessageFlag;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: 客户端接收到事件的处理
 * @Author: LiangYang
 * @Date: 2022/4/27 下午10:19
 **/
@Slf4j
public class ClientGetEventHandler implements Runnable{
    private final Socket client2serverSocket;
    private final AESUtil aesUtil;

    public ClientGetEventHandler(Socket socket) {
        this.client2serverSocket = socket;
        log.info("Client\t开始事件监听");
        aesUtil = new AESUtil();
    }
    public void newConnectHandler(InputStream in) throws IOException {
        log.trace("Client\t进入新连接处理");
        int read;
        byte[] portEncryptedBytes = new byte[16];
        if (!IOReadUtil.readFixedLength(in, portEncryptedBytes)) {
            log.warn("Server\t新连接事件异常, 端口号读取异常");
            return;
        }
        byte[] remotePortBytes = aesUtil.decrypt(portEncryptedBytes);   // 解密出云端端口号，长度为4个字节

        int encryptedNameLength = in.read();
        if (encryptedNameLength <= 0) {
            log.warn("Server\t新连接事件异常，encryptedNameLength={}", encryptedNameLength);
            return;
        }
        byte[] encryptedNameBytes = new byte[encryptedNameLength];
        if (!IOReadUtil.readFixedLength(in, encryptedNameBytes)){
            log.warn("Server\t新连接事件异常，远程socket信息数据长度不足");
            return;
        }
        byte[] nameBytes = aesUtil.decrypt(encryptedNameBytes);

        int remotePort = ByteUtil.byteArrayToInt(remotePortBytes);
        String localIp = ClientMapUtil.remotePortMap.get(remotePort).getLocalIp(); // 获取该云端端口对应本地局域网的机器的ip和端口
        int localPort = ClientMapUtil.remotePortMap.get(remotePort).getLocalPort();
        Socket socket = new Socket(localIp, localPort); // 建立连接
        // TODO 判断是否连接成功 并反馈
        new Thread(new Client2ServerForwarder(socket, client2serverSocket), "C2S_forward-"+socket).start();
        ClientMapUtil.socketLanMap.put(socket, nameBytes);
        ClientMapUtil.socketStringLanMap.put(new String(nameBytes), socket);
    }

    public void forward(InputStream in) throws IOException {
        log.trace("Client\t处理转发事件");

        int encryptedNameBytesLength = in.read(); // 携带的加密后名称字节数组长度
        if (encryptedNameBytesLength <= 0) {
            log.warn("Client\t转发事件异常，socket名称长度未指定或非法");
            return;
        }
        byte[] encryptedNameBytes = new byte[encryptedNameBytesLength];
        if (!IOReadUtil.readFixedLength(in, encryptedNameBytes)) {
            log.warn("Client\t转发事件异常，socket名称获取失败");
            return;
        }
        byte[] nameBytes = aesUtil.decrypt(encryptedNameBytes);

        byte[] encryptedDataLengthBytes = new byte[4];
        if (!IOReadUtil.readFixedLength(in, encryptedDataLengthBytes)) {
            log.warn("Client\t转发事件异常，消息体长度获取异常");
            return;
        }

        byte[] encryptedData = new byte[ByteUtil.byteArrayToInt(encryptedDataLengthBytes)];
        if (!IOReadUtil.readFixedLength(in, encryptedData)) {
            log.warn("Client\t转发事件异常,消息体获取异常，需要{}", encryptedData.length);
            return;
        }
        byte[] data = aesUtil.decrypt(encryptedData);

        Socket socketLan = ClientMapUtil.socketStringLanMap.get(new String(nameBytes));
        if (socketLan==null || socketLan.isClosed()){
            log.warn("Client\t转发事件异常"+new String(nameBytes)+"对应的socketLan"+(socketLan==null?"为null":socketLan+"已关闭"));
            return;
        }
        OutputStream out = socketLan.getOutputStream();
        out.write(data);
//        out.flush();
        log.trace("Client\t本次转发事件完成，消息体长度解密前{}/后{},转发携带{}，转发至{}", encryptedData.length, data.length, new String(nameBytes), socketLan);
    }
    public void closeConnect(InputStream in) throws IOException {
        log.trace("Client\t处理关闭连接事件");
        int nameCount = in.read();
        if (nameCount == -1) {
            log.warn("Client\t关闭连接事件异常，socket名称长度未指定");
            return;
        }
        byte[] encryptedNameBytes = new byte[nameCount];
        if (!IOReadUtil.readFixedLength(in, encryptedNameBytes)) {
            log.warn("Client\t关闭连接事件异常，socket名称获取失败");
            return;
        }
        byte[] nameBytes = aesUtil.decrypt(encryptedNameBytes);
        Socket socketLan = ClientMapUtil.socketStringLanMap.get(new String(nameBytes));
        ClientMapUtil.socketStringLanMap.remove(new String(nameBytes));
        ClientMapUtil.socketLanMap.remove(socketLan);
        socketLan.close();
        log.info("Client\t处理关闭连接事件完成，成功关闭并移除socketLan{}", socketLan);
    }
    @Override
    public void run() {
        while (client2serverSocket.isConnected() && !client2serverSocket.isClosed()) {
            try {
                InputStream in = client2serverSocket.getInputStream();
                int eventIndex;
                eventIndex = in.read();
                if (eventIndex==-1) {
                    log.warn("Client\tsocket对应的输入流关闭: {}，将关闭socket ",client2serverSocket);
                    client2serverSocket.close();
                }
                log.trace("Client\t收到事件: " + MessageFlag.getComment(eventIndex));

                if (eventIndex == MessageFlag.eventNewConnect){
                    newConnectHandler(in);
                } else if (eventIndex == MessageFlag.eventCloseConnect){
                    closeConnect(in);
                } else if (eventIndex == MessageFlag.eventForward){
                    forward(in);
                }else {
                    log.warn("Client\t无效事件索引： " + eventIndex);
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            client2serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Client\t与服务器的连接中断，退出");
    }
}
