package com.liang.proxy;

import com.liang.common.IOReadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午11:14
 **/
@Slf4j
public class Socks5ProxyHandler implements Runnable {
    public Socket socketProxyTransfer;
    public String remoteAddress;
    public int remotePort;
    public Socks5ProxyHandler(Socket socketProxyTransfer) {
        this.socketProxyTransfer = socketProxyTransfer;
    }
    public boolean handleSocks5Handshake(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[2];
        int len = in.read(bytes);
        if (len != 2) {
            return false;
        }
        if (bytes[0] != 0x05) {
            return false;
        }
        int nMethods = bytes[1];
        if (nMethods>0){
            bytes = new byte[nMethods];
            if (!IOReadUtil.readFixedLength(in, bytes)){
                return false;
            }
        }
        byte[] response = new byte[2];
        response[0] = 0x05;
        out.write(response);
        return true;
    }
    private String bytesToIpv4(byte[] addrBytes) {
        return String.format("%d.%d.%d.%d", addrBytes[0] & 0xff, addrBytes[1] & 0xff, addrBytes[2] & 0xff, addrBytes[3] & 0xff);
    }
    private String bytesToDomain(byte[] addrBytes) {
        StringBuilder domainString = new StringBuilder(addrBytes.length);
        for (byte addrByte : addrBytes) {
            domainString.append((char) addrByte);
        }
        return domainString.toString();
    }
    private String bytesToIpv6(byte[] addrBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i != 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", addrBytes[i]));
        }
        return sb.toString();
    }
    public boolean handleSock5Connection(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[4];
        if (!IOReadUtil.readFixedLength(in, bytes)){
            return false;
        }
        if (bytes[0] != 0x05) {
            return false;
        }
        if (bytes[1] != 0x01) {
            log.trace("Socks5\t暂不支持的cmd");
            return false;
        }
        if (bytes[2] != 0x00) {
            return false;
        }
        if (bytes[3] == 0x01) {
            byte[] ipv4Bytes =  new byte[4];
            if (!IOReadUtil.readFixedLength(in, ipv4Bytes)){
                return false;
            }
            remoteAddress = bytesToIpv4(ipv4Bytes);
        } else if (bytes[3] == 0x03) {
            byte[] domainBytes = new byte[1];
            if (!IOReadUtil.readFixedLength(in, domainBytes)){
                return false;
            }
            int domainLength = domainBytes[0];
            byte[] domainBytes2 = new byte[domainLength];
            if (!IOReadUtil.readFixedLength(in, domainBytes2)){
                return false;
            }
            remoteAddress = bytesToDomain(domainBytes2);
        } else if (bytes[3] == 0x04) {
            byte[] ipv6Bytes = new byte[16];
            if (!IOReadUtil.readFixedLength(in, ipv6Bytes)){
                return false;
            }
            remoteAddress = bytesToIpv6(ipv6Bytes);
        } else {
            return false;
        }
        byte[] portBytes = new byte[2];
        if (!IOReadUtil.readFixedLength(in, portBytes)){
            return false;
        }
        remotePort = (portBytes[0] & 0xff) << 8 | (portBytes[1] & 0xff);

        byte[] response = new byte[10];
        response[0] = 0x05;
        response[1] = 0x00;
        response[2] = 0x00;
        response[3] = 0x01;
        response[4] = 0x00;
        response[5] = 0x00;
        response[6] = 0x00;
        response[7] = 0x00;
        response[8] = 0x00;
        response[9] = 0x00;
        out.write(response);
        return true;
    }

    @Override
    public void run() {
        Socket agentSocket = null;
        try {
            InputStream input = socketProxyTransfer.getInputStream();
            OutputStream output = socketProxyTransfer.getOutputStream();
            if (handleSocks5Handshake(input, output)){
                if (handleSock5Connection(input, output)){
                    agentSocket = new Socket(remoteAddress, remotePort); // 代理socket 即真正访问目标资源的socket
                    Thread th1 = new Thread(new StreamForwardHandler(input, agentSocket.getOutputStream()), "Socks5Forward" + socketProxyTransfer + "-->" + agentSocket);
                    Thread th2 = new Thread(new StreamForwardHandler(agentSocket.getInputStream(), output), "Socks5Forward" + agentSocket + "-->" + socketProxyTransfer);
                    th1.start();
                    th2.start();
                    th1.join();
                    th2.join();
                } else {
                    log.trace("Socks5\t连接失败");
                }
            } else {
                log.trace("Socks5\t握手失败");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.trace("Socks5\t关闭本次代理连接{}",socketProxyTransfer);
        try {
            socketProxyTransfer.close();
            if (agentSocket!=null){
                agentSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
