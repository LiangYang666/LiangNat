package com.liang.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/26 下午3:47
 **/
public class AllowedIpUtil {
    static public HashSet<String> ipSets = new HashSet<>();
    public static boolean checkIpBelongCIDR(String cidrIp, String ip) throws UnknownHostException {
        String[] cidrIpArr = cidrIp.split("/");
        String cidrIpPrefix = cidrIpArr[0];
        int cidrIpPrefixLen = Integer.parseInt(cidrIpArr[1]);
        byte[] cidrIpBytes = InetAddress.getByName(cidrIpPrefix).getAddress();
        byte[] IpBytes = InetAddress.getByName(ip).getAddress();
        int cidrIpInt = getIntFromByte(cidrIpBytes);
        int IpInt = getIntFromByte(IpBytes);
        int mask = 0xffffffff << (32 - cidrIpPrefixLen);
        return (cidrIpInt & mask) == (IpInt & mask);
    }

    private static int getIntFromByte(byte[] address) {
        int addressInt = 0;
        for (int i = 0; i < address.length; i++) {
            addressInt += address[i] << (8 * (address.length - i - 1));
        }
        return addressInt;
    }
}
